package car.example.bean;

public class MyBean {

    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public void showMessage() {
        System.out.println("Message: " + message);
    }

    @Override
    public String toString() {
        return "MyBean{" +
                "message='" + message + '\'' +
                '}';
    }
}


//Bean yarattıktan sonra spring containerın bu objecti yönetebilmesi için bir xml dosyası = config dosyası hazırlamamız gerekiyor .