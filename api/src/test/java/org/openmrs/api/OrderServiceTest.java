/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openmrs.Allergy;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Condition;
import org.openmrs.Diagnosis;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.DrugIngredient;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.MedicationDispense;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderAttribute;
import org.openmrs.OrderAttributeType;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderGroup;
import org.openmrs.OrderGroupAttribute;
import org.openmrs.OrderGroupAttributeType;
import org.openmrs.OrderSet;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientState;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProgramAttributeType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttributeType;
import org.openmrs.Relationship;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.builder.DrugOrderBuilder;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.api.db.hibernate.HibernateAdministrationDAO;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.api.impl.OrderServiceImpl;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.hl7.HL7InError;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.order.OrderUtil;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.orders.TimestampOrderNumberGenerator;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.parameter.OrderSearchCriteriaBuilder;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.Order.Action.DISCONTINUE;
import static org.openmrs.Order.FulfillerStatus.COMPLETED;
import static org.openmrs.test.OpenmrsMatchers.hasId;
import static org.openmrs.test.TestUtil.containsId;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {

	private static final String OTHER_ORDER_FREQUENCIES_XML = "org/openmrs/api/include/OrderServiceTest-otherOrderFrequencies.xml";

	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";

	private static final String ORDER_GROUP_ATTRIBUTES = "org/openmrs/api/include/OrderServiceTest-createOrderGroupAttributes.xml";

	private static final String ORDER_ATTRIBUTES = "org/openmrs/api/include/OrderServiceTest-createOrderAttributes.xml";

	@Autowired
	private ConceptService conceptService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PatientService patientService;

	@Autowired
	private EncounterService encounterService;

	@Autowired
	private ProviderService providerService;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private OrderSetService orderSetService;

	@Autowired
	private MessageSourceService messageSourceService;
	
	@Autowired
	private VisitService visitService;
	
	@BeforeEach
	public void setUp(){
		executeDataSet(ORDER_ATTRIBUTES);
		executeDataSet(ORDER_GROUP_ATTRIBUTES);
	}
}
