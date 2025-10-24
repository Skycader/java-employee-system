// src/Employee.java
public interface Employee {
    String getId();
    String getFullName();
    String getBirthday();
    String getPosition();
    double getSalary();

    void setFullName(String fullName);
    void setBirthday(String birthday);
    void setPosition(String position);
    void setSalary(double salary);
}