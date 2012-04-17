package models.eventstream;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;

public class RdfEvent extends Event {

	private Model model;

	public RdfEvent(String title, String content) {
		super(title, content);
	}

	public RdfEvent(String title, Model model) {
		super(title, "");
		this.model = model;
	}

	@Override
	public String getContent() {
		return this.model.serialize(Syntax.Turtle);
	}	
}
