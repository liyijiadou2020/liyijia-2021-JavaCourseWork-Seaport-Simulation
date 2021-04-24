package pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public enum CargoType {
    LOOSE,
    LIQUID,
    CONTAINER
}
