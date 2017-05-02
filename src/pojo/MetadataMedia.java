package pojo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Gabo on 02/05/2017.
 */
public class MetadataMedia {
    private StringProperty key;
    private ObjectProperty value;

    public String getKey() {
        return keyProperty().get();
    }

    public StringProperty keyProperty() {
        if (key == null) key = new SimpleStringProperty(this, "key");
        return key;
    }

    public void setKey(String key) {
        keyProperty().set(key);
    }

    public Object getValue() {
        return valueProperty().get();
    }

    public ObjectProperty valueProperty() {
        if (value == null) value = new SimpleObjectProperty(this, "value");
        return value;
    }

    public void setValue(Object value) {
        valueProperty().set(value);
    }

    public MetadataMedia(String key, Object value) {
        setKey(key);
        setValue(value);
    }

    @Override
    public String toString() {
        return "MetadataMedia{" +
                "key=" + key.getValue() +
                ", value=" + value.getValue() +
                '}';
    }
}
