package org.nutz.doc.txt;

import org.nutz.doc.ConvertAdaptor;
import org.nutz.doc.ConvertContext;
import org.nutz.doc.RenderLogger;
import org.nutz.doc.ZDocException;
import org.nutz.doc.zdoc.ZDocSetParser;

public class TextAdaptor implements ConvertAdaptor {

	@Override
	public void adapt(ConvertContext context) throws ZDocException {
		context.setParser(new ZDocSetParser(context.getIndexml()));
		context.setRender(new TextDocSetRender(".txt", new RenderLogger()));
	}

}