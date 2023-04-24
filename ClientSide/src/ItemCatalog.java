import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class ItemCatalog {
    // Item Info
    private final String itemType;
    private final String title;
    private final String author;
    private final String desc;

    // Item Status
    private boolean checkedOut;
    private String currUser;
    private String holdUser;
    private LocalDate lastCheckedOut;
    private LocalDate dueDate;
    public CopyOnWriteArrayList<String> prevUsers;

    public ItemCatalog(String itemType, String title, String author, String desc) {
        this.itemType = itemType;
        this.title = title;
        this.author = author;
        this.desc = desc;
        this.checkedOut = false;
        this.currUser = null;
        this.lastCheckedOut = null;
        this.dueDate = null;
        this.prevUsers = new CopyOnWriteArrayList<String>();
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

    public CopyOnWriteArrayList<String> getPrevUsers() {
        return prevUsers;
    }

    public boolean isHeld(){
        return this.holdUser != null;
    }


    // ADD AUTHENTICATION HERE TO checkOut and returnItem METHODS !!!
    public synchronized void checkOut(String name){
        if(!this.checkedOut){
            this.currUser = name;
            this.lastCheckedOut = LocalDate.now();
            this.dueDate = LocalDate.now().plusMonths(1);
            this.checkedOut = true;
//            return true;
        }
    }

    public synchronized void holdItem(String name){
        if(this.checkedOut){
            this.holdUser = name;
        }
    }

    public synchronized void processHold(){
        if(this.holdUser != null){
            this.currUser = this.holdUser;
            this.lastCheckedOut = LocalDate.now();
            this.dueDate = LocalDate.now().plusMonths(1);
            this.checkedOut = true;
            this.holdUser = null;
        }
    }

    public synchronized String returnItem(){
        if(this.checkedOut){
            String a = currUser;
            // Uncheck out
//            this.prevUsers.add(currUser);
            this.currUser = null;
            this.dueDate = null;
            this.checkedOut = false;
            return a;
        }
        return null;
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
