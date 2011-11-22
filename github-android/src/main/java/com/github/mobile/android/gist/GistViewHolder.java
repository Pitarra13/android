package com.github.mobile.android.gist;

import com.github.mobile.android.R;
import com.madgag.android.listviews.ViewHolder;
import org.eclipse.egit.github.core.Gist;

import android.view.View;
import android.widget.TextView;

/**
 * View holder for a {@link Gist}
 */
public class GistViewHolder implements ViewHolder<Gist> {

    private final TextView title;

    /**
     * Create view holder for a {@link Gist}
     *
     * @param v
     */
    public GistViewHolder(View v) {
        title = (TextView) v.findViewById(R.id.gv_list_item_id);
    }

    @Override
    public void updateViewFor(Gist gist) {
        String id = gist.getId();
        title.setText(id);
    }
}
