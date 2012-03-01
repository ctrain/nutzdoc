package org.nutz.doc.website;

import static org.nutz.lang.util.Tag.tag;

import org.nutz.doc.DocRender;
import org.nutz.doc.html.HtmlRenderSupport;
import org.nutz.doc.meta.ZBlock;
import org.nutz.doc.meta.ZDoc;
import org.nutz.lang.util.Tag;

/**
 * 渲染 HTML 的 DOM 结构，并返回根元素 HTML，它下面一定有 HEAD 以及 BODY
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
class WebsiteDocRender extends HtmlRenderSupport implements DocRender<Tag> {

	WebsiteDocRender() {
		this.skip_index_block = true;
		this.hide_anchor_for_header = true;
		this.hide_top_for_each_section = true;
	}

	@Override
	public Tag render(ZDoc doc) {
		// 准备基本的 DOM 结构
		Tag html = Tag.tag("html");
		Tag head = html.add("head");
		head.add("title").setText(doc.getTitle());
		Tag body = html.add("body");

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

		return html;
	}

}
