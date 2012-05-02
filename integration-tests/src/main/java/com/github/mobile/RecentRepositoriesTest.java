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
package com.github.mobile;

import static com.github.mobile.ui.repo.RecentRepositories.MAX_SIZE;
import android.test.AndroidTestCase;

import com.github.mobile.ui.repo.RecentRepositories;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;

/**
 * Unit tests of {@link RecentRepositories}
 */
public class RecentRepositoriesTest extends AndroidTestCase {

	/**
	 * Verify bad input
	 */
	public void testBadInput() {
		User org = new User().setId(20);
		RecentRepositories recent = new RecentRepositories(getContext(), org);
		assertFalse(recent.contains((IRepositoryIdProvider) null));
		assertFalse(recent.contains((String) null));
		assertFalse(recent.contains(""));
	}

	/**
	 * Verify eviction
	 */
	public void testMaxReached() {
		User org = new User().setId(20);
		RecentRepositories recent = new RecentRepositories(getContext(), org);

		for (int i = 0; i < MAX_SIZE; i++) {
			String id = "owner/repo" + i;
			recent.add(id);
			assertTrue(recent.contains(id));
		}

		recent.add("owner/repoLast");
		assertTrue(recent.contains("owner/repoLast"));
		assertFalse(recent.contains("owner/repo0"));

		for (int i = 1; i < MAX_SIZE; i++) {
			String id = "owner/repo" + i;
			assertTrue(recent.contains(id));
		}
	}

	/**
	 * Verify input/output to disk of {@link RecentRepositories} state
	 */
	public void testIO() {
		User org = new User().setId(20);
		RecentRepositories recent1 = new RecentRepositories(getContext(), org);
		String id = "owner/repo";
		recent1.add(id);
		assertTrue(recent1.contains(id));
		recent1.save();
		RecentRepositories recent2 = new RecentRepositories(getContext(), org);
		assertTrue(recent2.contains(id));
	}

}
