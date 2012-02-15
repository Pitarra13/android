package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListAdapter;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.HttpImageGetter;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a issue's description and comments
 */
public class IssueFragment extends ListLoadingFragment<FullIssue> {

    private LoaderCallbacks<List<FullIssue>> loadListener;

    private IRepositoryIdProvider repository;

    private int id;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private View bodyView;

    private HttpImageGetter imageGetter;

    @Inject
    private AvatarHelper avatarHelper;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imageGetter = new HttpImageGetter(getActivity());
    }

    /**
     * @param id
     * @return this fragment
     */
    public IssueFragment setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Update issue body area with latest values from issue
     *
     * @param issue
     * @return this fragment
     */
    public IssueFragment updateIssue(final Issue issue) {
        if (bodyView != null)
            new IssueBodyViewHolder(imageGetter, bodyView).updateViewFor(issue);
        return this;
    }

    /**
     * @param repository
     * @return this fragment
     */
    public IssueFragment setRepository(IRepositoryIdProvider repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param loadListener
     * @return this fragment
     */
    public IssueFragment setLoadListener(LoaderCallbacks<List<FullIssue>> loadListener) {
        this.loadListener = loadListener;
        return this;
    }

    @Override
    public Loader<List<FullIssue>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<FullIssue>>(getActivity()) {
            @Override
            public List<FullIssue> loadInBackground() {
                try {
                    Issue issue = store.refreshIssue(repository, id);
                    List<Comment> comments;
                    if (issue.getComments() > 0)
                        comments = service.getComments(repository, id);
                    else
                        comments = Collections.emptyList();
                    return Collections.singletonList(new FullIssue(issue, comments));
                } catch (IOException e) {
                    showError(e, string.error_issue_load);
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<FullIssue> items) {
        if (items.isEmpty() && getListAdapter() != null)
            return getListAdapter();

        if (bodyView != null)
            getListView().removeHeaderView(bodyView);

        Issue issue = !items.isEmpty() ? items.get(0).getIssue() : null;
        List<Comment> comments = !items.isEmpty() ? items.get(0).getComments() : Collections.<Comment> emptyList();

        if (issue != null) {
            bodyView = getActivity().getLayoutInflater().inflate(layout.issue_view_body, null);
            new IssueBodyViewHolder(imageGetter, bodyView).updateViewFor(issue);
            getListView().addHeaderView(bodyView);
        }

        return new ViewHoldingListAdapter<Comment>(comments, ViewInflator.viewInflatorFor(getActivity(),
                layout.comment_view_item), ReflectiveHolderFactory.reflectiveFactoryFor(CommentViewHolder.class,
                avatarHelper, imageGetter));
    }

    @Override
    public void onLoadFinished(Loader<List<FullIssue>> loader, List<FullIssue> items) {
        super.onLoadFinished(loader, items);
        if (loadListener != null)
            loadListener.onLoadFinished(loader, items);
    }

    @Override
    public void onLoaderReset(Loader<List<FullIssue>> listLoader) {
        super.onLoaderReset(listLoader);
        if (loadListener != null)
            loadListener.onLoaderReset(listLoader);
    }
}