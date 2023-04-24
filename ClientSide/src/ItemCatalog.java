import java.time.LocalDate;
import java.util.ArrayList;

public class ItemCatalog {
    // Item Info
    private final String itemType;
    private final String title;
    private final String author;
    private final String desc;

    // Item Status
    private boolean checkedOut;
    private String currUser;
    private LocalDate lastCheckedOut;
    private LocalDate dueDate;
    public ArrayList<String> prevUsers;

    public ItemCatalog(String itemType, String title, String author, String desc) {
        this.itemType = itemType;
        this.title = title;
        this.author = author;
        this.desc = desc;
        this.checkedOut = false;
        this.currUser = null;
        this.lastCheckedOut = null;
        this.dueDate = null;
        this.prevUsers = new ArrayList<String>();
    }

    // Getters
    public String getItemType() {
        return itemType;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public String getCurrUser() {
        return currUser;
    }

    public LocalDate getLastCheckedOut() {
        return lastCheckedOut;
    }

    public ArrayList<String> getPrevUsers() {
        return prevUsers;
    }


    // ADD AUTHENTICATION HERE TO checkOut and returnItem METHODS !!!
    public synchronized void checkOut(String name){
        System.out.println("AOFJOWAO FJAWF OAJWOF JOAWJFO AWOFJ AWOF JA");
        if(this.checkedOut){
//            return false;
        }
        else{
            this.currUser = name;
            this.lastCheckedOut = LocalDate.now();
            this.dueDate = LocalDate.now().plusMonths(1);
            this.checkedOut = true;
//            return true;
        }
    }

    public synchronized void returnItem(){
        System.out.println("AAAAAFJOJFIEJFJOEJFJEOFJEJOFJ");
        if(this.checkedOut){
            // Uncheck out
            System.out.println("LOLOLOLOLOLOLOLOLOL");
            this.prevUsers.add(currUser);
            System.out.println("step 1");
            this.currUser = null;
            System.out.println("step 2");
            this.dueDate = null;
            System.out.println("step 3");
            this.checkedOut = false;
            System.out.println("step 4");
        }
        System.out.println("DONE!!!!");
    }

    public void checkDueDate(){
        if (this.dueDate.equals(LocalDate.now())){
            // Uncheck out
            this.prevUsers.add(this.currUser);
            this.currUser = null;
            this.dueDate = null;
            this.checkedOut = false;
        }
    }
}
