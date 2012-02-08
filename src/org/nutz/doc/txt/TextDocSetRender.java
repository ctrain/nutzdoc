package org.nutz.doc.txt;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.nutz.doc.DocSetRender;
import org.nutz.doc.RenderLogger;
import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.meta.ZDocSet;
import org.nutz.doc.meta.ZItem;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.Node;

public class TextDocSetRender implements DocSetRender {

	private TextDocRender render;
	private String suffix;
	private RenderLogger L;

	public TextDocSetRender(String suffix, RenderLogger L) {
		render = new TextDocRender();
		this.suffix = suffix;
		this.L = L;
	}

	/**
	 * @param dest
	 *            - show be a directory
	 */
	public void render(String destPath, ZDocSet set) throws IOException {
		File dest = new File(Disks.normalize(destPath));
		if (!dest.exists())
			Files.makeDir(dest);
		else if (dest.isFile())
			throw Lang.makeThrow("Dest: '%' should be a directory!", dest);
		Stopwatch sw = new Stopwatch();
		L.log1("Rending zdoc from : %s", dest);
		sw.start();
		renderDocSet(dest, set);
		sw.stop();
		L.log1("All finished in %s", sw.toString());
	}

	private void renderDocSet(File dest, ZDocSet set) throws IOException {
		Iterator<Node<ZItem>> it = set.root().iterator();
		while (it.hasNext()) {
			Node<ZItem> node = it.next();
			ZItem zi = node.get();
			if (zi instanceof ZDoc)
				renderDoc(dest, set.getSrc(), (ZDoc) zi);
		}
	}

	private void renderDoc(File dest, String srcRoot, ZDoc doc) throws IOException {
		L.log1("<Doc: '%s'>", doc.getSource());
		// Write Text to file
		L.log3("write Text");
		String s = render.render(doc).toString();
		File newDocFile = new File(dest.getAbsolutePath()
									+ "/"
									+ doc.getSource().substring(srcRoot.length()));
		newDocFile = Files.renameSuffix(newDocFile, suffix);

		if (!newDocFile.exists())
			Files.createNewFile(newDocFile);
		Lang.writeAll(Streams.fileOutw(newDocFile), s);

	}

}
