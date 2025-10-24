// src/EmployeeImpl.java
public class EmployeeImpl implements Employee {
    private final String id;
    private String fullName;
    private String birthday;
    private String position;
    private double salary;

    public EmployeeImpl(String id, String fullName, String birthday, String position, double salary) {
        this.id = id;
        this.fullName = fullName;
        this.birthday = birthday;
        this.position = position;
        this.salary = salary;
    }

    @Override public String getId() { return id; }
    @Override public String getFullName() { return fullName; }

    @Override
    public String getBirthday() { return birthday; }

    @Override public String getPosition() { return position; }
    @Override public double getSalary() { return salary; }

    @Override public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override public void setPosition(String position) { this.position = position; }
    @Override public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return String.format("%s | %-30s | %-30s | %-15s | %.0f", id, fullName, birthday, position, salary);
    }

    public String toFileString() {
        return String.format("%s|%s|%s|%s|%.0f", id, fullName, birthday, position, salary);
    }
}