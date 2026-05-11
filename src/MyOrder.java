public class MyOrder {
    private int id;
    private String roomName;
    private String startTime;
    private String endTime;
    private String status;
    private int price;

    public MyOrder(int id, String roomName, String startTime, String endTime, String status, int price) {
        this.id = id;
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.price = price;
    }

    public int getId() { return id; }
    public String getRoomName() { return roomName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public int getPrice() { return price; }
}