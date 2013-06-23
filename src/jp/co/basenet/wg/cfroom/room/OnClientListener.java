package jp.co.basenet.wg.cfroom.room;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public interface OnClientListener {

	/** @see ConnectException */
	void onConnectException();

	/** @see SocketTimeoutException */
	void onTimeoutException();

	/** @see SocketException */
	void onSocketException();

	void onConnected();

	void onReceive(Object obj);
	void onExited();
}
