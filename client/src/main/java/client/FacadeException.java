package client;

public class FacadeException extends Exception{

    private final int statusCode;

    public FacadeException(int statusCode,String message){
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
