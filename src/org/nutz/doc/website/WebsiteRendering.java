package org.nutz.doc.website;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

import org.nutz.doc.RenderLogger;
import org.nutz.doc.meta.ZD;
import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.meta.ZDocSet;
import org.nutz.doc.meta.ZEle;
import org.nutz.doc.meta.ZFolder;
import org.nutz.doc.meta.ZIndex;
import org.nutz.doc.meta.ZItem;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Tag;

class WebsiteRendering {

	private ZDocSet set;

	private WebsiteDocRender render;

	private RenderLogger L;

	private String suffix;

	/**
	 * 主文件的模板
	 */
	private File indexTmpl;

	/**
	 * zDoc 源目录
	 */
	private File setHome;

	/**
	 * 目标输出目录
	 */
	private File dest;

	/**
	 * 存放所有的图片文件
	 */
	private File images;

	WebsiteRendering(String destPath, ZDocSet set, RenderLogger L) {
		this.set = set;
		this.setHome = Files.findFile(Disks.absolute(set.getSrc()));
		this.set.setSrc(setHome.getAbsolutePath());
		this.render = new WebsiteDocRender();
		this.suffix = ".html";
		this.L = L;
		dest = new File(Disks.normalize(destPath));
		images = Files.createDirIfNoExists(dest.getAbsolutePath() + "/imgs");
		indexTmpl = Files.findFile(set.getSrc() + "/index.tmpl");
		if (null == indexTmpl)
			throw Lang.makeThrow("Can not find index.tmpl in '%s'", setHome);
	}

	/**
	 * 将一个 zDoc 的文档中所有的图片，移动到图片目录，并修改链接
	 * 
	 * @throws IOException
	 */
	private void normalizeDocumentImages(Node<ZItem> nd) throws IOException {
		// 如果是 zFolder，则继续递归
		if (nd.get() instanceof ZFolder) {
			for (Node<ZItem> sub : nd.getChildren()) {
				normalizeDocumentImages(sub);
			}
		}
		// zDoc 转换 ...
		else if (nd.get() instanceof ZDoc) {
			ZDoc doc = (ZDoc) nd.get();
			for (ZEle img : doc.root().getImages()) {
				// 判断 img 文件
				File imgFile = img.getSrc().getFile();
				if (null == imgFile)
					continue;
				// 得到相对与整个 Site 的路径
				String rePath = Disks.getRelativePath(setHome, imgFile);
				// Copy
				L.log2("Copy image '%s'", rePath);
				Files.copyFile(imgFile, Files.getFile(images, rePath));

				// 修改 Image 的 src
				img.setSrc(ZD.refer("imgs/" + rePath));
			}
		}
	}

	private void renderDoc(Node<ZItem> nd) throws IOException {
		// 如果是 zFolder，则继续递归
		if (nd.get() instanceof ZFolder) {
			for (Node<ZItem> sub : nd.getChildren()) {
				renderDoc(sub);
			}
		}
		// zDoc 转换 ...
		else if (nd.get() instanceof ZDoc) {
			ZDoc doc = (ZDoc) nd.get();
			Tag html = render.render(doc);
			// 得到相对路径
			String rePath = Disks.getRelativePath(set.getSrc(), doc.getSource());
			rePath = Files.renameSuffix(rePath, suffix);
			// 创建新文件
			File f = Files.createFileIfNoExists(Files.getFile(dest, rePath).getAbsolutePath());
			L.log2(	"Render zDoc '%s' => '%s'",
					Disks.getRelativePath(set.getSrc(), doc.getSource()),
					Disks.getRelativePath(dest.getAbsolutePath(), f.getAbsolutePath()));
			Files.write(f, html.toString());
		}
	}

	void render() throws IOException {
		Stopwatch sw = Stopwatch.begin();

		L.log1("Generate set '%s' => '%s'", set.getSrc(), dest);
		/*
		 * 循环所有的节点，处理链接的 Image
		 */
		L.log1("Find %d top docs, normalize images", set.root().countChildren());
		for (Node<ZItem> top : set.root().getChildren()) {
			this.normalizeDocumentImages(top);
		}
		L.log1("... done.");

		/*
		 * 循环生成每一个 zDoc
		 */
		L.log1("Rendering zDoc ...");
		for (Node<ZItem> top : set.root().getChildren()) {
			this.renderDoc(top);
		}
		L.log1("... done.");

		/*
		 * 生成整个 DocSet 的索引，并将 index.tmpl 输出成 index.html
		 */
		L.log1("Rendering index.tmpl");
		Node<ZIndex> node = set.createIndexTable();
		// 更新链接的后缀
		Iterator<Node<ZIndex>> it = node.iterator();
		while (it.hasNext()) {
			Node<ZIndex> zi = it.next();
			if (zi.get().hasHref()) {
				zi.get().setHref(Files.renameSuffix(zi.get().getHref(), suffix));
			}
		}
		// 生成 DOM
		Tag tag = render.renderIndexTable(node);
		// 渲染模板
		String html = Segments.read(indexTmpl).set("html", tag.toString()).render().toString();
		File indexHtml = Files.createFileIfNoExists(dest + "/index" + suffix);
		Files.write(indexHtml, html);
		L.log1("... done.");

		// 最后将 home 的所有 js 和 css copy 过去
		L.log1("copy css and js files ...");
		copyResourceFiles(dest, setHome);
		L.log1("...done.");

		// 结束
		sw.stop();
		L.log1("All done in '%s'", sw.toString());

	}

	private void copyResourceFiles(final File dest, File src) throws IOException {
		L.log2("Check : %s", dest);
		// File files
		L.log2("Finding resource file ...");
		File[] fs = src.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					return f.getName().toLowerCase().matches("^(.*[.])(css|js)$");
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
			L.log2(	"%s => %s",
					Disks.getRelativePath(setHome, f),
					Disks.getRelativePath(dest, newFile));
			Files.copyFile(f, newFile);
		}
		// Find sub Folders
		fs = src.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					if (f.getName().charAt(0) == '.')
						return false;
					else
						return true;
				return false;
			}
		});
		// Copy -^ recuring...
		for (File f : fs) {
			File newFile = new File(dest.getAbsolutePath() + "/" + f.getName());
			copyResourceFiles(newFile, f);
		}
		// 如果发现 _rs 目录，copy 其中所有的 img
		if (Files.getFile(dest, "_rs").isDirectory()) {
			File srcRs = Files.getFile(setHome, "_rs");
			File[] imgs = srcRs.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().matches("^(.*[.])(png|jpg|jpeg|gif)$");
				}
			});
			for (File img : imgs) {
				File newImg = Files.getFile(dest, "_rs/" + img.getName());
				L.log2(	"%s => %s",
						Disks.getRelativePath(setHome, img),
						Disks.getRelativePath(dest, newImg));
				Files.copyFile(img, newImg);
			}
		}
	}
}
