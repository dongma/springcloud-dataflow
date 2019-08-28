package org.microservice.dataflow.exception;

/**
 * @author: Sam Ma
 * When Item in csv file is invalid, Throw InvalidItemException exception, this record will be skipped.
 */
public class InvalidItemException extends Exception {

    public InvalidItemException() {
    }

    public InvalidItemException(String message) {
        super(message);
    }

}
