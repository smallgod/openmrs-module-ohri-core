package org.openmrs.module.ohricore;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.module.ohricore.Item;
import org.openmrs.module.ohricore.api.dao.OHRICoreDao;
import org.openmrs.module.ohricore.api.impl.OHRICoreServiceImpl;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

/**
 * This is a unit test, which verifies logic in OHRICoreService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class OHRICoreServiceTest {
	
	@InjectMocks
	OHRICoreServiceImpl basicModuleService;
	
	@Mock
	OHRICoreDao dao;
	
	@Mock
	UserService userService;
	
	@BeforeEach
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void saveItem_shouldSetOwnerIfNotSet() {
		
		//Given
		Item item = new Item();
		item.setDescription("some description");
		
		when(dao.saveItem(item)).thenReturn(item);
		
		User user = new User();
		when(userService.getUser(1)).thenReturn(user);
		
		//When
		basicModuleService.saveItem(item);
		
		//Then
		MatcherAssert.assertThat(item, hasProperty("owner", is(user)));
	}
}
