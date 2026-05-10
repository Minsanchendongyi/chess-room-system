public class Room {
    private String name;
    private int capacity;
    private int price;
    private String status;

    // 构造方法
    public Room(String name, int capacity, int price, String status) {
        this.name = name;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
    }

    // Getter 方法（让其他类能读取这些属性）
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getPrice() { return price; }
    public String getStatus() { return status; }
}