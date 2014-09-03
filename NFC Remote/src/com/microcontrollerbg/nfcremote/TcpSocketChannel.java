package com.microcontrollerbg.nfcremote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * This a helper class, to bundle the socket operations in a unified manner. It
 * can be instantiated, possibly in multiple instances. It is not meant to be
 * inherited from, or exported. It should therefore throw low-level exceptions,
 * not HarcHardwareException.
 */
public class TcpSocketChannel {
	private String hostIp;
	private int portNumber;
	private boolean verbose;
	private int timeout;
	private TcpSocketPort.ConnectionMode connectionMode;
	private Socket socket = null;
	private PrintStream outStream = null;
	private BufferedReader inStream = null;

	public TcpSocketChannel(String hostIp, int portNumber, int timeout,
			boolean verbose, TcpSocketPort.ConnectionMode connectionMode) {
		this.hostIp = hostIp;
		this.portNumber = portNumber;
		this.timeout = timeout;
		this.verbose = verbose;
		this.connectionMode = connectionMode;
	}

	public void connect() throws IOException {
		if (socket == null || !socket.isConnected()) {
			socket = new Socket();
			if (verbose)
				System.err.println("Connecting socket to " + hostIp);

			socket.connect(new InetSocketAddress(InetAddress.getByName(hostIp),
					portNumber), timeout);
			socket.setSoTimeout(timeout);
			socket.setKeepAlive(connectionMode == TcpSocketPort.ConnectionMode.keepAlive);
		}

		if (outStream == null)
			outStream = new PrintStream(socket.getOutputStream());

		if (inStream == null)
			inStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
	}

	public void close(boolean force) throws IOException {
		if (force || connectionMode == TcpSocketPort.ConnectionMode.justInTime) {
			if (outStream != null) {
				outStream.close();
				outStream = null;
			}
			if (inStream != null) {
				inStream.close();
				inStream = null;
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
	}

	public PrintStream getOut() {
		return outStream;
	}

	public BufferedReader getIn() {
		return inStream;
	}

	public boolean isValid() {
		return socket != null;
	}

	public void setTimeout(int timeout) throws SocketException {
		this.timeout = timeout;
		socket.setSoTimeout(timeout);
	}

	public void setVerbosity(boolean verbose) {
		this.verbose = verbose;
	}
}
