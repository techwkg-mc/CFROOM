package jp.co.basenet.wg.cfroom.room;

import java.io.Serializable;

public class SerObject<T> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8436076334544215509L;
	
	private T object;
	
	public SerObject(T object) {
		this.object = object;
	}
	
	public void setObject(T object) {
		this.object = object;
	}
	
	public T getObject() {
		return this.object;
	}
}
