package org.nutz.doc.txt;

import java.util.List;

import org.nutz.doc.DocRender;
import org.nutz.doc.meta.ZBlock;
import org.nutz.doc.meta.ZDoc;
import org.nutz.lang.Strings;

public class TextDocRender extends TextRenderSupport implements DocRender<StringBuilder> {

	@Override
	public StringBuilder render(ZDoc doc) {
		StringBuilder sb = new StringBuilder();

		// 标题
		if (!Strings.isBlank(doc.getTitle())) {
			sb.append(Strings.dup('#', 60));
			sb.append("\n# ");
			sb.append("\n#    《").append(doc.getTitle()).append("》");
			sb.append("\n# ");
		}

		// 元数据
		for (String mnm : doc.metaNames()) {
			if ("title".equalsIgnoreCase(mnm))
				continue;
			List<String> mvs = doc.getMetaList(mnm);
			for (String mv : mvs)
				sb.append("\n# " + mnm + ": " + mv);
		}

		// 空行
		sb.append("\n");

		// Render doc contents
		ZBlock[] ps = doc.root().children();
		for (ZBlock p : ps)
			renderBlock(sb, p);

		// 空行
		sb.append("\n\n");

		// 结束
		sb.append(Strings.dup('-', 30)).append(" The End ").append(Strings.dup('-', 30));

		return sb;
	}
}
