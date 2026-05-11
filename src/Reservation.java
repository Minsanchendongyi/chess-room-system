import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int userId;
    private int roomId;
    private String roomName;
    private String startTime;
    private String endTime;
    private String status;

    public Reservation(int id, int userId, int roomId, String roomName,
                       String startTime, String endTime, String status) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getter 方法
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
}