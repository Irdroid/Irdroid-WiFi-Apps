package com.microcontrollerbg.irmacro;

/*
 Copyright (C) 2009-2012 Bengt Martensson.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or (at
 your option) any later version.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program. If not, see http://www.gnu.org/licenses/.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;


/**
 * A <a href="http://www.lirc.org">LIRC</a> client, talking to a remote LIRC
 * server through a TCP port.
 */
@SuppressLint("ShowToast")
public class LircClient {

	private String lircServerIp;
	public final static int lircDefaultPort = 8765;
	private int lircPort;
	public final static String defaultLircIP = "127.0.0.1"; //
	public final static int defaultTimeout = 5000; // WinLirc can be really
	Context context; // slow...

	private boolean verbose = true;
	private int timeout = defaultTimeout;

	public LircClient(String hostname, int port, boolean verbose, int timeout) {
		this.timeout = timeout;
		lircServerIp = (hostname != null) ? hostname : defaultLircIP;
		lircPort = port;
		this.verbose = verbose;
	}

	public LircClient(String hostname, boolean verbose, int timeout) {
		this(hostname, lircDefaultPort, verbose, timeout);
	}

	public LircClient(String hostname, boolean verbose) {
		this(hostname, verbose, defaultTimeout);
	}

	LircClient(String hostname) {
		this(hostname, false);
	}

	public static class LircIrTransmitter extends Transmitter {
		private int[] transmitters;

		private LircIrTransmitter() {
			transmitters = null;
		}

		private LircIrTransmitter(int[] transmitters) {
			this.transmitters = transmitters;
		}

		private LircIrTransmitter(int selectedTransmitter) {
			if (selectedTransmitter >= 0) {
				transmitters = new int[1];
				transmitters[0] = selectedTransmitter;
			}
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < transmitters.length; i++)
				s.append(' ').append(transmitters[i]);

			return s.toString();
		}
	}

	public LircIrTransmitter newTransmitter() {
		return new LircIrTransmitter();
	}

	public LircIrTransmitter newTransmitter(int port) {
		return new LircIrTransmitter(port);
	}

	public LircIrTransmitter newTransmitter(String port) {
		return (port == null || port.isEmpty()) ? new LircIrTransmitter()
				: new LircIrTransmitter(Integer.parseInt(port));
	}

	private LircIrTransmitter lircIrTransmitter = new LircIrTransmitter();

	public void setVerbosity(boolean verbosity) {
		this.verbose = verbosity;
	}

	private final static int P_BEGIN = 0;
	private final static int P_MESSAGE = 1;
	private final static int P_STATUS = 2;
	private final static int P_DATA = 3;
	private final static int P_N = 4;
	private final static int P_DATA_N = 5;
	private final static int P_END = 6;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private static class BadPacketException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		BadPacketException() {
			super();
		}

		@SuppressWarnings("unused")
		BadPacketException(final String message) {
			super(message);
		}
	}

	protected ArrayList<String> sendCommand(String packet, boolean oneWord)
			throws IOException {
		if (verbose) {
			System.err.println("Sending command `" + packet + "' to Lirc@"
					+ lircServerIp);
		}

		TcpSocketChannel tcpSocketChannel = new TcpSocketChannel(lircServerIp,
				lircPort, timeout, verbose,
				TcpSocketPort.ConnectionMode.justInTime);
		tcpSocketChannel.connect();

		PrintStream outToServer = tcpSocketChannel.getOut();
		BufferedReader inFromServer = tcpSocketChannel.getIn();
		if (outToServer == null || inFromServer == null)
			throw new IOException(
					"Could not open socket connection to LIRC server "
							+ lircServerIp);
		// this.context = c;
		outToServer.print(packet + '\n');

		ArrayList<String> result = new ArrayList<String>();
		int status = 0;
		try {
			int state = P_BEGIN;
			int n = 0;
			boolean done = false;
			// int errno = 0;
			int dataN = -1;

			while (!done) {
				String string = inFromServer.readLine();
				// System.out.println("***"+string+"***"+state);
				if (string == null) {
					done = true;
					status = -1;
				} else {
					switch (state) {
					case P_BEGIN:
						if (!string.equals("BEGIN")) {
							System.err.println("!begin");
							continue;
						}
						state = P_MESSAGE;
						break;
					case P_MESSAGE:
						if (!string.equals(packet)) {
							state = P_BEGIN;
							continue;
						}
						state = P_STATUS;
						break;
					case P_STATUS:
						if (string.equals("SUCCESS")) {
							status = 0;
						} else if (string.equals("END")) {
							status = 0;
							done = true;
						} else if (string.equals("ERROR")) {
							System.err.println("command failed: " + packet);
							status = -1;
						} else {
							throw new BadPacketException();
						}
						state = P_DATA;
						break;
					case P_DATA:
						if (string.equals("END")) {
							done = true;
							break;
						} else if (string.equals("DATA")) {
							state = P_N;
							break;
						}
						throw new BadPacketException();
					case P_N:
						// errno = 0;
						dataN = Integer.parseInt(string);
						// result = new String[data_n];

						state = dataN == 0 ? P_END : P_DATA_N;
						break;
					case P_DATA_N:
						if (verbose) {
							System.err.println(string);
						}
						// Different LIRC servers seems to deliver commands in
						// different
						// formats. Just take the last word.
						// result[n++] = one_word ?
						// string.replaceAll("\\S*\\s+", "") : string;

						result.add(oneWord ? string.replaceAll("\\S*\\s+", "")
								: string);
						n++;
						if (n == dataN) {
							state = P_END;
						}
						break;
					case P_END:
						if (string.equals("END")) {
							done = true;
						} else {
							throw new BadPacketException();
						}
						break;
					default:
						assert false : "Unhandled case";
						break;
					}
				}
			}
		} catch (BadPacketException e) {
			System.err.println("bad return packet");
			status = -1;
		} catch (SocketTimeoutException e) {
			System.err.println("Sockettimeout Lirc: " + e.getMessage());
			result = null;
			status = -1;
		} catch (IOException e) {
			System.err.println("Couldn't read from " + lircServerIp);
			status = -1;
			// System.exit(1);
		} finally {
		}
		if (verbose) {
			System.err.println(status == 0 ? "Lirc command succeded."
					: "Lirc command failed.");
		}
		if (result != null && !result.isEmpty() && verbose) {
			System.err.println("result[0] = " + result.get(0));
		}

		return status == 0 ? result : null;
	}

	public boolean sendIrCommand(String remote, String command, int count,
			Transmitter transmitter) throws Exception {
		try {
			return setTransmitters(transmitter)
					&& sendCommand("SEND_ONCE " + remote + " " + command + " "
							+ (count - 1), false) != null;
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public boolean sendIr1Command(String remote, String command, int count)
			throws Exception {
		try {

			sendCommand("SEND_ONCE " + remote + " " + command + " "
					+ (count - 1), false);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
		return verbose;
	}

	public boolean sendIrCommand(String remote, String command, int count,
			int connector) throws Exception {
		return sendIrCommand(remote, command, count, new LircIrTransmitter(
				connector));
	}

	/*
	 * public boolean sendIr(String remote, command_t cmd, int count) throws
	 * Exception { return send_command("SEND_ONCE " + remote + " " + cmd + " " +
	 * (count - 1), false) != null; }
	 */

	public boolean sendIrCommandRepeat(String remote, String command,
			Transmitter transmitter) throws Exception {
		try {
			return setTransmitters(transmitter)
					&& sendCommand("SEND_START " + remote + " " + command,
							false) != null;
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public boolean stopIr(String remote, String command, Transmitter transmitter)
			throws Exception {
		try {
			return setTransmitters(transmitter)
					&& sendCommand("SEND_STOP " + remote + " " + command, false) != null;
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public boolean stopIr(String remote, String command, int port)
			throws Exception {
		return stopIr(remote, command, new LircIrTransmitter(port));
	}

	public ArrayList<String> getRemotes() throws Exception {
		try {
			return sendCommand("LIST", false);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public ArrayList<String> getCommands(String remote) throws Exception {
		try {
			return sendCommand("LIST " + remote, true);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	// Questionable
	/*
	 * public String getRemoteCommand(String remote, String command) throws
	 * Exception { try { String[] result = sendCommand("LIST " + remote + " " +
	 * command, false); return result != null ? result[0] : null; } catch
	 * (IOException ex) { throw new Exception(ex); } }
	 */

	/**
	 * Sends the SET_TRANSMITTER command to the LIRC server.
	 * 
	 * @param transmitter
	 * @return
	 * @throws Exception
	 */
	public boolean setTransmitters(Transmitter transmitter) throws Exception {
		LircIrTransmitter trans = (LircIrTransmitter) transmitter;
		// if (!lircIrTransmitter.equals(trans)) {
		lircIrTransmitter = trans;
		return setTransmitters();
		// } else
		// return true;
	}

	public boolean setTransmitters(int port) throws Exception {
		return setTransmitters(new LircIrTransmitter(port));
	}

	private boolean setTransmitters(/* int[] trans */) throws Exception {
		String s = "SET_TRANSMITTERS" + lircIrTransmitter.toString();
		try {
			return sendCommand(s, false) != null;
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public String getVersion() throws Exception {
		try {
			ArrayList<String> result = sendCommand("VERSION", false);
			return (result == null || result.isEmpty()) ? null : result.get(0);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * Dummy implementation, always returns true
	 * 
	 * @return true
	 */

	public boolean isValid() {
		return true;
	}

}
