/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.commit;

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

/**
 * Activity to display a commit
 */
public class CommitViewActivity extends DialogFragmentActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param id
     * @return intent
     */
    public static Intent createIntent(final Repository repository,
            final String id) {
        Builder builder = new Builder("commits.VIEW");
        builder.add(EXTRA_BASE, id);
        builder.repo(repository);
        return builder.toIntent();
    }

    @InjectExtra(EXTRA_BASE)
    private String id;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.commit);

        Intent intent = getIntent();
        Repository repository = (Repository) intent
                .getSerializableExtra(EXTRA_REPOSITORY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(id);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());
    }
}
