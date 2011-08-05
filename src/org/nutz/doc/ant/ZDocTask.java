package org.nutz.doc.ant;

import java.io.IOException;

import org.nutz.doc.ConvertAdaptor;
import org.nutz.doc.ConvertContext;
import org.nutz.doc.Doc;
import org.nutz.doc.ZDocException;
import org.nutz.doc.meta.ZDocSet;
import org.nutz.lang.Lang;

public class ZDocTask {

	private String src;

	private String dest;

	private String suffix;

	private String indexXml;
	
	private String imageAddress;
	
	private String indexWikiName;

	public void execute() throws IOException, ZDocException {
		if (src == null || dest == null)
			throw Lang.makeThrow("src or dest can't be null");
		if (suffix == null)
			suffix = "html";
		ConvertAdaptor adaptor = Doc.convertAdaptorMap.get(suffix);
		if (adaptor == null)
			throw Lang.makeThrow("Unknow suffix", suffix);
		ConvertContext cc = new ConvertContext();
		cc.setSrc(src);
		cc.setDest(dest);
		if (indexXml != null)
			cc.addArg(indexXml);
		if (indexWikiName != null)
			cc.addArg(indexWikiName);
		if (imageAddress != null)
			cc.addArg(imageAddress);
		adaptor.adapt(cc);
		ZDocSet set = cc.getParser().parse(cc.getSrc());
		cc.getRender().render(cc.getDest(), set);
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setIndexXml(String indexXml) {
		this.indexXml = indexXml;
	}
	
	public void setImageAddress(String imageAddress) {
		this.imageAddress = imageAddress;
	}
	
	public void setIndexWikiName(String indexWikiName) {
		this.indexWikiName = indexWikiName;
	}
}
