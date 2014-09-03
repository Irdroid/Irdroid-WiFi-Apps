package com.microcontrollerbg.irmacro;

/*
 Copyright (C) 2012 Bengt Martensson.

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

import java.io.IOException;
import java.net.SocketException;

public class TcpSocketPort {
	public enum ConnectionMode {
		keepAlive, justInTime;
	};

	TcpSocketChannel tcpSocketChannel;

	public TcpSocketPort(String hostIp, int portNumber, int timeout,
			boolean verbose, ConnectionMode connectionMode) {
		tcpSocketChannel = new TcpSocketChannel(hostIp, portNumber, timeout,
				verbose, connectionMode);
	}

	public void close() throws Exception {
		try {
			tcpSocketChannel.close(true);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public void sendString(String str) throws Exception {
		try {
			tcpSocketChannel.connect();
			tcpSocketChannel.getOut().print(str);
			tcpSocketChannel.close(false);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public String readString() throws Exception {
		try {
			tcpSocketChannel.connect();
			String result = tcpSocketChannel.getIn().readLine();
			tcpSocketChannel.close(false);
			return result;
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

	public boolean isValid() {
		return tcpSocketChannel.isValid();
	}

	public String getVersion() {
		return "";
	}

	public void setTimeout(int timeout) {
		try {
			tcpSocketChannel.setTimeout(timeout);
		} catch (SocketException ex) {
		}
	}

	public void setVerbosity(boolean verbose) {
		tcpSocketChannel.setVerbosity(verbose);
	}

	public static void main(String[] args) {
		try {
			TcpSocketPort port = new TcpSocketPort("denon", 23, 2000, true,
					ConnectionMode.keepAlive);
			port.sendString("MVDOWN\r");
			String result = port.readString();
			System.out.println(result);
			port.close();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}
