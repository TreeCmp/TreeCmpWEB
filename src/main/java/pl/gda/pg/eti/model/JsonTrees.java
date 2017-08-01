package pl.gda.pg.eti.model;

import java.io.Serializable;
import java.util.UUID;

public class JsonTrees implements Serializable {
	public static final long serialVersionUID = 1L;
	public int firstTreeId;
	public int secondTreeId;
	public String firstTreeNewick;
	public String secondTreeNewick;
}