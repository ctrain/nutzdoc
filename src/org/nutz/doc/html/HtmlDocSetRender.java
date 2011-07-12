package org.nutz.doc.html;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.doc.DocSetRender;
import org.nutz.doc.RenderLogger;
import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.meta.ZDocSet;
import org.nutz.doc.meta.ZD;
import org.nutz.doc.meta.ZEle;
import org.nutz.doc.meta.ZIndex;
import org.nutz.doc.meta.ZItem;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Streams;
import org.nutz.lang.segment.CharSegment;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Tag;

public class HtmlDocSetRender implements DocSetRender {

	private HtmlDocRender render;
	private String suffix;
	private RenderLogger L;

	public HtmlDocSetRender(String suffix, RenderLogger L) {
		render = new HtmlDocRender();
		this.suffix = suffix;
		this.L = L;
	}

	private void copyResourceFiles(	final File dest,
									File src,
									final List<File> csss,
									final List<File> jss) throws IOException {
		L.log2("Check : %s", dest);
		// File files
		L.log2("Finding resource file ...");
		File[] fs = src.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					String name = f.getName();
					if (name.matches("^.*[.]css$")) {
						csss.add(f);
						return true;
					}
					if (name.matches("^.*[.]js$")) {
						jss.add(f);
						return true;
					}
					return name.toLowerCase().matches("^(.*[.])(html|htm)$");
				}
				return false;
			}
		});
		L.log2("Found %d", fs.length);
		if (fs.length == 0)
			return;
		if (Files.makeDir(dest))
			L.log2("[OK] It don't existed, create it!");
		else
			L.log2("[KO] It alread existed!");
		// Copy
		for (File f : fs) {
			File newFile = new File(dest.getAbsolutePath() + "/" + f.getName());
			L.log2("%s => %s", f, newFile);
			Files.copyFile(f, newFile);
		}
		// Find sub Folders
		fs = src.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					if (f.getName().charAt(0) == '.' || f.getName().charAt(0) == '_')
						return false;
					else
						return true;
				return false;
			}
		});
		// Copy -^ recuring...
		for (File f : fs) {
			File newFile = new File(dest.getAbsolutePath() + "/" + f.getName());
			copyResourceFiles(newFile, f, csss, jss);
		}
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
		// It just copy the html/js/css, for the image files, we will copy
		// them later. Only copy image files refered by ZDoc file.
		List<File> csss = new LinkedList<File>();
		List<File> jss = new LinkedList<File>();
		L.log1("Copy resource files...");

		copyResourceFiles(dest, set.checkSrcDir(), csss, jss);
		renderDocSet(dest, set, csss, jss);
		renderIndexHtml(dest, set);
		sw.stop();
		L.log1("All finished in %s", sw.toString());
	}

	private void renderIndexHtml(File dest, ZDocSet set) {
		// And then, let's check index.html existed in the source directory root
		File indexHtmlFile = findIndexHtml(dest);
		if (null == indexHtmlFile) {
			L.log1("Fail to find index.html");
			return;
		}
		Segment indexHtml = new CharSegment(Files.read(indexHtmlFile));

		L.log1("Rendering index.html ... ");
		Node<ZIndex> node = set.createIndexTable();
		// Update all links, make the extenstion to suffix
		Iterator<Node<ZIndex>> it = node.iterator();
		while (it.hasNext()) {
			Node<ZIndex> zi = it.next();
			if (zi.get().hasHref()) {
				zi.get().setHref(Files.renameSuffix(zi.get().getHref(), suffix));
			}
		}
		// rendering tags
		Tag tag = render.renderIndexTable(node);
		indexHtml.set("html", tag);
		File f = new File(dest.getAbsolutePath() + "/" + indexHtmlFile.getName());
		Lang.writeAll(Streams.fileOutw(f), indexHtml.toString());
	}

	private File findIndexHtml(File dest) {
		File[] htmls = dest.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".htm") || pathname.getName().endsWith(".html");
			}
		});
		for (File f : htmls) {
			String s = Lang.readAll(Streams.fileInr(f));
			CharSegment re = new CharSegment(s);
			if (re.contains("html"))
				return f;
		}
		return null;
	}

	private void renderDocSet(File dest, ZDocSet set, List<File> csss, List<File> jss)
			throws IOException {
		Iterator<Node<ZItem>> it = set.root().iterator();
		while (it.hasNext()) {
			Node<ZItem> node = it.next();
			ZItem zi = node.get();
			if (zi instanceof ZDoc)
				renderDoc(dest, csss, jss, set.getSrc(), (ZDoc) zi);
		}
	}

	private void renderDoc(File dest, List<File> csss, List<File> jss, String srcRoot, ZDoc doc)
			throws IOException {
		File f;
		doc.setAttr("css", csss);
		doc.setAttr("js", jss);
		File src = Files.findFile(doc.getSource());
		int pos = src.getParent().length() + 1;

		L.log1("<Doc: '%s'>", doc.getSource());
		// 替换 Link 的后缀
		List<ZEle> links = doc.root().getLinks();
		L.log3("Found %d links", links.size());
		for (ZEle link : links) {
			f = link.getHref().getFile();
			if (null != f)
				if (f.getAbsolutePath().length() > pos) {
					String path = f.getAbsolutePath().substring(pos);
					String newPath = Files.renameSuffix(path, suffix);
					L.log4(" %s => %s", path, newPath);
					if (link.getHref().hasInner())
						newPath += "#" + link.getHref().getInner();
					link.setHref(ZD.refer(newPath));
				}
		}
		// Write HTML to file
		L.log3("write HTML");
		String s = render.render(doc).toString();
		File newDocFile = new File(dest.getAbsolutePath()
									+ "/"
									+ doc.getSource().substring(srcRoot.length()));
		newDocFile = Files.renameSuffix(newDocFile, suffix);

		if (!newDocFile.exists())
			Files.createNewFile(newDocFile);
		Lang.writeAll(Streams.fileOutw(newDocFile), s);
		// Copy Images && change the image to new path
		List<ZEle> images = doc.root().getImages();
		L.log3("Found %d images", images.size());
		for (ZEle img : images) {
			f = img.getSrc().getFile();
			if (null != f) {
				String path = doc.getRelativePath(f.getAbsolutePath());
				File newImg = new File(newDocFile.getParent() + "/" + path);
				L.log4("Copy: %s => %s", f, newImg);
				Files.copyFile(f, newImg);
				img.setSrc(ZD.refer(doc.getRelativePath(f.getAbsolutePath())));
				L.log4("update src to: %s", img.getSrc().toString());
			}
		}
	}
}
