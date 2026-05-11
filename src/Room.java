public class Room {
    private int id;
    private String name;
    private int capacity;
    private int price;
    private String status;

    public Room(int id, String name, int capacity, int price, String status) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getPrice() { return price; }
    public String getStatus() { return status; }
}