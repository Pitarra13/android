package com.github.mobile.android.ui.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_CLOSE;
import static com.github.mobile.android.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_MILESTONE_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_REOPEN;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENTS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RefreshAnimation;
import com.github.mobile.android.SingleChoiceDialogFragment;
import com.github.mobile.android.async.AuthenticatedUserTask;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.core.issue.FullIssue;
import com.github.mobile.android.issue.IssueHeaderViewHolder;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.ui.DialogResultListener;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ErrorHelper;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;

/**
 * Fragment to display an issue
 */
public class IssueFragment extends RoboSherlockFragment implements DialogResultListener {

    @Inject
    private ContextScopedProvider<IssueService> service;

    private String repositoryName;

    private String repositoryOwner;

    private int issueNumber;

    private List<Comment> comments;

    private RepositoryId repositoryId;

    private Issue issue;

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private IssueStore store;

    @InjectView(android.R.id.list)
    private ListView list;

    private View headerView;

    private IssueHeaderViewHolder headerHolder;

    private View loadingView;

    private RefreshAnimation refreshAnimation = new RefreshAnimation();

    private EditMilestoneTask milestoneTask;

    private EditAssigneeTask assigneeTask;

    private EditLabelsTask labelsTask;

    private EditStateTask stateTask;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        repositoryName = args.getString(EXTRA_REPOSITORY_NAME);
        repositoryOwner = args.getString(EXTRA_REPOSITORY_OWNER);
        repositoryId = RepositoryId.create(repositoryOwner, repositoryName);
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER);
        comments = (List<Comment>) args.getSerializable(EXTRA_COMMENTS);

        DialogFragmentActivity dialogActivity = (DialogFragmentActivity) getActivity();

        milestoneTask = new EditMilestoneTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        assigneeTask = new EditAssigneeTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        labelsTask = new EditLabelsTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        stateTask = new EditStateTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setFastScrollEnabled(true);
        list.addHeaderView(headerView, null, false);

        issue = store.getIssue(repositoryId, issueNumber);

        if (issue != null)
            headerHolder.updateViewFor(issue);
        else
            headerView.setVisibility(GONE);

        if (issue == null || (issue.getComments() > 0 && comments == null))
            list.addHeaderView(loadingView, null, false);

        List<Comment> initialComments = comments;
        if (initialComments == null)
            initialComments = Collections.emptyList();
        list.setAdapter(new ViewHoldingListAdapter<Comment>(initialComments, ViewInflator.viewInflatorFor(
                getActivity(), layout.comment_view_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                CommentViewHolder.class, avatarHelper)));

        if (issue != null && comments != null)
            updateList(issue, comments);

        refreshIssue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.issue_view, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);

        headerView = inflater.inflate(layout.issue_header, null);

        headerView.findViewById(id.ll_milestone).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    milestoneTask.prompt(issue.getMilestone());
            }
        });

        headerView.findViewById(id.ll_assignee).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null) {
                    User assignee = issue.getAssignee();
                    assigneeTask.prompt(assignee != null ? assignee.getLogin() : null);
                }
            }
        });

        headerView.findViewById(id.ll_labels).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    labelsTask.prompt(issue.getLabels());
            }
        });

        headerView.findViewById(id.ll_state).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    stateTask.confirm(STATE_OPEN.equals(issue.getState()));
            }
        });

        headerHolder = new IssueHeaderViewHolder(headerView, avatarHelper, getResources());
        loadingView = inflater.inflate(layout.comment_load_item, null);
    }

    private void refreshIssue() {
        new AuthenticatedUserTask<FullIssue>(getActivity()) {

            public FullIssue run() throws Exception {
                Issue issue = store.refreshIssue(repositoryId, issueNumber);
                List<Comment> comments;
                if (issue.getComments() > 0)
                    comments = service.get(getContext()).getComments(repositoryId, issueNumber);
                else
                    comments = Collections.emptyList();
                return new FullIssue(issue, comments);
            }

            protected void onException(Exception e) throws RuntimeException {
                ErrorHelper.show(getContext(), e, string.error_issue_load);
            }

            protected void onSuccess(FullIssue fullIssue) throws Exception {
                issue = fullIssue.getIssue();
                comments = fullIssue;
                getArguments().putSerializable(EXTRA_COMMENTS, fullIssue);
                updateList(fullIssue.getIssue(), fullIssue);
            }

            protected void onFinally() throws RuntimeException {
                refreshAnimation.stop();
            }
        }.execute();
    }

    private void updateList(Issue issue, List<Comment> comments) {
        list.removeHeaderView(loadingView);
        headerView.setVisibility(VISIBLE);
        headerHolder.updateViewFor(issue);

        ViewHoldingListAdapter<Comment> adapter = getRootAdapter();
        if (adapter != null)
            adapter.setList(comments);
    }

    @SuppressWarnings("unchecked")
    private ViewHoldingListAdapter<Comment> getRootAdapter() {
        ListAdapter adapter = list.getAdapter();
        if (adapter == null)
            return null;
        adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        if (adapter instanceof ViewHoldingListAdapter<?>)
            return (ViewHoldingListAdapter<Comment>) adapter;
        else
            return null;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (Activity.RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            milestoneTask.edit(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            assigneeTask.edit(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_LABELS_UPDATE:
            labelsTask.edit(arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_CLOSE:
            stateTask.edit(true);
            break;
        case ISSUE_REOPEN:
            stateTask.edit(false);
            break;
        }
    }
}