package org.nutz.doc.html;

import java.io.File;
import java.util.List;

import org.nutz.doc.DocRender;
import org.nutz.doc.meta.*;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Tag;

import static org.nutz.lang.util.Tag.*;

public class HtmlDocRender extends HtmlRenderSupport implements DocRender<StringBuilder> {

	@SuppressWarnings("unchecked")
	public StringBuilder render(ZDoc doc) {
		Tag html = tag("html");
		Tag head = tag("head");
		html.add(head);
		head.add(tag("meta").attr("HTTP-EQUIV", "Content-Type").attr(	"CONTENT",
																		"text/html; charset=UTF-8"));
		if (!Strings.isBlank(doc.getTitle()))
			head.add(tag("title").add(text(doc.getTitle())));
		// <link rel="stylesheet" type="text/css">
		if (doc.hasAttr("css")) {
			List<File> csss = (List<File>) doc.getAttr("css");
			for (File css : csss) {
				String path = doc.getRelativePath(css.getAbsolutePath());
				head.add(Tag.tag("link")
							.attr("href", path)
							.attr("rel", "stylesheet")
							.attr("type", "text/css"));
			}
		}
		// <script language="javascript">
		if (doc.hasAttr("js")) {
			List<File> jss = (List<File>) doc.getAttr("js");
			for (File js : jss) {
				String path = doc.getRelativePath(js.getAbsolutePath());
				head.add(Tag.tag("script").attr("src", path).attr("language", "Javascript"));
			}
		}
		Tag body = tag("body");
		// Add doc header
		body.add(tag("a").attr("name", "top"));
		body.add(Tag.tag("div").attr("class", "zdoc_header").add(Tag.text(doc.getTitle())));
		// Add author
		if (doc.hasAuthor())
			body.add(appendAuthorTag(doc, Tag.tag("div").attr("class", "zdoc_author")));

		html.add(body);
		Tag container = tag("div").attr("class", "zdoc_body");
		body.add(container);

		// Add doc footer
		if (doc.hasAuthor())
			body.add(appendAuthorTag(doc, Tag.tag("div").attr("class", "zdoc_footer")));

		// Render doc contents
		ZBlock[] ps = doc.root().children();
		for (ZBlock p : ps)
			renderBlock(container, p);
		/*
		 * At last, we render HTML as string
		 */
		return new StringBuilder().append(COMMON_INFO).append("\n").append(html.toString());
	}
}
