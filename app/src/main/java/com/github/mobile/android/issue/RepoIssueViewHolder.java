package com.github.mobile.android.issue;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.TypefaceHelper;
import com.madgag.android.listviews.ViewHolder;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.egit.github.core.Issue;

/**
 * View holder for an issue in a repository
 */
public class RepoIssueViewHolder implements ViewHolder<Issue> {

    /**
     * Find the maximum number of digits in the given issue numbers
     *
     * @param issues
     * @return max digits
     */
    public static int computeMaxDigits(Iterable<Issue> issues) {
        int max = 1;
        for (Issue issue : issues)
            max = Math.max(max, (int) Math.log10(issue.getNumber()) + 1);
        return max;
    }

    /**
     * Measure size of widest issue number in issues
     *
     * @param number
     * @param issues
     * @return number width
     */
    public static int measureNumberWidth(TextView number, Iterable<Issue> issues) {
        int maxNumberCount = computeMaxDigits(issues);
        Paint paint = new Paint();
        paint.setTypeface(number.getTypeface());
        paint.setTextSize(number.getTextSize());
        char[] text = new char[maxNumberCount + 1];
        Arrays.fill(text, '0');
        text[0] = '#';
        return Math.round(paint.measureText(text, 0, text.length));
    }

    private final TextView number;

    private final TextView title;

    private final ImageView gravatar;

    private final TextView creation;

    private final TextView comments;

    private final TextView pullRequestIcon;

    private final AvatarHelper helper;

    private final int flags;

    private final AtomicInteger numberWidth;

    /**
     * Create view holder
     *
     * @param v
     * @param helper
     * @param numberWidth
     */
    public RepoIssueViewHolder(View v, AvatarHelper helper, AtomicInteger numberWidth) {
        number = (TextView) v.findViewById(id.tv_issue_number);
        flags = number.getPaintFlags();
        title = (TextView) v.findViewById(id.tv_issue_title);
        gravatar = (ImageView) v.findViewById(id.iv_gravatar);
        creation = (TextView) v.findViewById(id.tv_issue_creation);
        comments = (TextView) v.findViewById(id.tv_issue_comments);

        pullRequestIcon = (TextView) v.findViewById(id.tv_pull_request_icon);
        TypefaceHelper.setOctocons(pullRequestIcon, (TextView) v.findViewById(id.tv_comment_icon));

        this.helper = helper;
        this.numberWidth = numberWidth;
    }

    @Override
    public void updateViewFor(Issue issue) {
        number.setText("#" + issue.getNumber());
        if (issue.getClosedAt() != null)
            number.setPaintFlags(flags | STRIKE_THRU_TEXT_FLAG);
        else
            number.setPaintFlags(flags);
        number.getLayoutParams().width = numberWidth.get();

        gravatar.setImageDrawable(null);
        helper.bind(gravatar, issue.getUser());

        pullRequestIcon.setVisibility(issue.getPullRequest().getHtmlUrl() == null ? GONE : VISIBLE);

        title.setText(issue.getTitle());
        creation.setText(fromHtml("<b>" + issue.getUser().getLogin() + "</b> " + relativeTimeFor(issue.getCreatedAt())));
        comments.setText(Integer.toString(issue.getComments()));
    }
}
