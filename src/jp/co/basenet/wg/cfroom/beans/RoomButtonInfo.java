package jp.co.basenet.wg.cfroom.beans;

public class RoomButtonInfo {

    private int id;
    private String roomName;
    private String status;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
