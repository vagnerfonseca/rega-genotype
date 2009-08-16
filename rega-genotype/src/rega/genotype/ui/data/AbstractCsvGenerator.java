package rega.genotype.ui.data;

import java.io.PrintStream;

public abstract class AbstractCsvGenerator extends SaxParser {
	private PrintStream os;

	public AbstractCsvGenerator(PrintStream os) {
		this.os = os;
	}

	@Override
	public void endSequence() {
		writeLine(os);
	}
	
	public abstract void writeLine(PrintStream ps);
	

    protected String getCsvValue(String name) {
    	String val = getValue(name);
    	if(val==null)
    		val="";
    	return "\"" + val + "\"";
    }
}