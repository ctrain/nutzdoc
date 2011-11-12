package org.nutz.doc.chm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.nutz.doc.RenderLogger;
import org.nutz.doc.html.HtmlDocSetRender;
import org.nutz.doc.meta.ZDocSet;
import org.nutz.doc.meta.ZIndex;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Tag;

public class ChmDocSetRender extends HtmlDocSetRender {
	
	private ZDocSet set;
	
	private String destPath;

	public ChmDocSetRender(String suffix, RenderLogger L) {
		super(suffix, L);
	}

	@Override
	public void render(String destPath, ZDocSet set) throws IOException {
		this.destPath = new File(destPath).getAbsolutePath();
		this.set = set;
		//先输出标准的Html文档
		super.render(destPath, set);
		//生成Help workshop项目文件
		hhp();
		hhc();
		//hhk();
	}
	
	protected void hhp() {
		InputStream hhp = getClass().getResourceAsStream("/org/nutz/doc/chm/docs.hhp");
		try {
			Streams.writeAndClose(new FileOutputStream(destPath + "/docs.hhp"), hhp);
		} catch (FileNotFoundException e) {
			throw Lang.wrapThrow(e);
		}
	}
	
	protected void hhc() {
		Node<ZIndex> node = set.createIndexTable();
		// Update all links, make the extenstion to suffix
		Iterator<Node<ZIndex>> it = node.iterator();
		while (it.hasNext()) {
			Node<ZIndex> zi = it.next();
			if (zi.get().hasHref()) {
				zi.get().setHref(Files.renameSuffix(zi.get().getHref(), ".html"));
			}
		}
		renderIndex(node);
		Tag ul = Tag.tag("UL");
		for (Node<ZIndex> n : node.getChildren()) {
			ul.add(renderIndex(n));
		}
		String hhc_str = Streams.readAndClose(Streams.utf8r(getClass().getResourceAsStream("/org/nutz/doc/chm/docs.hhc")));
		Segment hhc = Segments.create(hhc_str);
		hhc.add("UL", ul.toString());
		try {
			Streams.writeAndClose(new FileOutputStream(destPath+"/docs.hhc"), hhc.render().toString().getBytes("GBK"));
		} catch (IOException e) {
			throw Lang.wrapThrow(e);
		}
	}
	
	protected Tag renderIndex(Node<ZIndex> node) {
		Tag li = Tag.tag("LI");
		Tag obj = Tag.tag("OBJECT", "type=text/sitemap");
		li.add(obj);
		Tag param_name = Tag.tag("param", "name=Name","value="+node.get().getText());
		obj.add(param_name);
		if (node.hasChild()) {
			Tag ui = Tag.tag("UL");
			for (Node<ZIndex> c : node.getChildren()) {
				ui.add(renderIndex(c));
			}
			li.add(ui);
		} else {
			Tag param_local = Tag.tag("param", "name=Local","value="+node.get().getHref().replace('/', '\\'));
			obj.add(param_local);
		}
		return li;
	}
	
	protected void hhk() {
		InputStream hhp = getClass().getResourceAsStream("/org/nutz/doc/chm/docs.hhk");
		try {
			Streams.writeAndClose(new FileOutputStream(destPath + "/docs.hhk"), hhp);
		} catch (FileNotFoundException e) {
			throw Lang.wrapThrow(e);
		}
	}
}
