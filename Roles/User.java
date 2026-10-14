package Roles;

public abstract class User {
    private String role;

    public User(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void viewAllIceCream() {

    }

    public void sortIceCream() {

    }

    public void searchIceCream() {

    }

    public abstract void landingPage();
}
