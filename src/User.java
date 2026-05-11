public class User {
    private int id;
    private String phone;
    private String password;
    private String name;
    private String level;

    public User(int id, String phone, String password, String name, String level) {
        this.id = id;
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.level = level;
    }

    // Getter 方法
    public int getId() { return id; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getLevel() { return level; }
}