package org.wikipedia.page.snippet;

import android.text.Html;

import org.wikipedia.page.PageActivity;
import org.wikipedia.page.PageViewFragmentInternal;
import org.wikipedia.page.Section;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Share the first paragraph text since no text was selected.
 */
public class NoTextSelectedShareAdapter extends ShareHandler {

    public NoTextSelectedShareAdapter(PageActivity activity) {
        super(activity);
    }

    public void share() {
        final PageViewFragmentInternal curPageFragment = getActivity().getCurPageFragment();
        if (curPageFragment == null) {
            return;
        }

        createFunnel();
        getFunnel().logShareTap(null);

        shareSnippet(getFirstParagraphText(curPageFragment), true);
    }

    private CharSequence getFirstParagraphText(final PageViewFragmentInternal curPageFragment) {
        Pattern pattern = Pattern.compile("<p>(.+)</p>");
        for (Section section : curPageFragment.getPage().getSections()) {
            final String sectionText = section.getContent();
            Matcher matcher = pattern.matcher(sectionText);
            if (matcher.find()) {
                return Html.fromHtml(matcher.group(1));
            }
        }
        return "";
    }
}
