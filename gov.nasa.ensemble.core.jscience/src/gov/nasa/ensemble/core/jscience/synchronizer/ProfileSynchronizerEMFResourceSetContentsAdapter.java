/*******************************************************************************
 * Copyright 2014 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package gov.nasa.ensemble.core.jscience.synchronizer;

import gov.nasa.ensemble.emf.SafeAdapter;
import gov.nasa.ensemble.emf.transaction.TransactionUtils;
import gov.nasa.ensemble.emf.util.EMFUtils;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Handle resource set changes which may be addition or removal of EMF Resources
 */
final class ProfileSynchronizerEMFResourceSetContentsAdapter extends SafeAdapter {
	private final ProfileSynchronizerEMFResourceContentsAdapter emfResourceContentsAdapter;
	private ResourceSet resourceSet;
	public ProfileSynchronizerEMFResourceSetContentsAdapter(final ResourceSet resourceSet, ProfileSynchronizer synchronizer) {
		emfResourceContentsAdapter = new ProfileSynchronizerEMFResourceContentsAdapter(synchronizer);
		this.resourceSet = resourceSet;
		resourceSet.eAdapters().add(this);
		TransactionUtils.writeIfNecessary(resourceSet, new Runnable() {
			@Override
			public void run() {
				EList<Resource> resources = resourceSet.getResources();
				for (Resource resource : resources) {
					addResource(resource);
				}
			}
		});
	}
	public void dispose() {
		if (resourceSet != null) {
			resourceSet.eAdapters().remove(this);
			TransactionUtils.reading(resourceSet, new Runnable() {
				@Override
				public void run() {
					EList<Resource> resources = resourceSet.getResources();
					for (Resource resource : resources) {
						removeResource(resource);
					}
				}
			});
			this.resourceSet = null;
		}
		if (emfResourceContentsAdapter != null) {
			emfResourceContentsAdapter.dispose();
		}
	}
	@Override
	protected void handleNotify(Notification notification) {
		for (Resource resource : EMFUtils.getRemovedObjects(notification, Resource.class)) {
			removeResource(resource);
		}
		for (Resource resource : EMFUtils.getAddedObjects(notification, Resource.class)) {
			addResource(resource);
		}
	}
	private void addResource(Resource resource) {
		emfResourceContentsAdapter.startWatching(resource);
	}
	private void removeResource(Resource resource) {
		emfResourceContentsAdapter.stopWatching(resource);
	}
}
