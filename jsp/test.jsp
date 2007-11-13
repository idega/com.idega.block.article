<?xml version="1.0"?>
<jsp:root
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:builder="http://xmlns.idega.com/com.idega.builder"
	xmlns:x="http://myfaces.apache.org/tomahawk" version="1.2">
	<jsp:directive.page contentType="text/html" pageEncoding="UTF-8" />
	<f:view>
		<builder:page id="builderpage_344" template="93">
			<builder:region id="left" label="left">
				<h:form id="form1" styleClass="rafverk">
					<x:div rendered="#{TilkynningVertakaBean.applicationInvalid || TilkynningVertakaBean.currentWorkingPlaceErrorMessageNotEmpty}" styleClass="errorLayer">
						<x:div rendered="#{TilkynningVertakaBean.applicationInvalid || TilkynningVertakaBean.currentWorkingPlaceErrorMessageNotEmpty}" styleClass="errorImage" />
						<x:htmlTag id="errorHeading" forceId="true" value="h1">
							<f:verbatim>Vinsamlega lagfærðu eftirfarandi villu/r</f:verbatim>
						</x:htmlTag>
						
						<h:outputText rendered="#{TilkynningVertakaBean.currentWorkingPlaceErrorMessageNotEmpty}" value="#{TilkynningVertakaBean.currentWorkingPlaceErrorMessage}" />
						<h:outputText value=" " />
						<h:commandLink rendered="#{TilkynningVertakaBean.currentWorkingPlaceErrorMessageNotEmpty}" value="Fara á  rangt skráðan reit" action="firstWizardPage" />
						<h:outputText rendered="#{TilkynningVertakaBean.applicationInvalid}" value="Sending mistókst: Fylla verður í alla skilyrta reiti. Þegar þú hefur lagfært villurnar smelltu þá aftur á 'Senda' hnappinn." />
						<x:div rendered="#{TilkynningVertakaBean.invalid['energyCompany'] != null}">
							<h:outputText value="Fyrsta skref, Orkuveitufyrirtæki" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['energyCompany']}" action="firstWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['energyConsumerName'] != null}">
							<h:outputText value="Fyrsta skref, Nafn orkukaupanda" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['energyConsumerName']}" action="firstWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['energyConsumerPersonalId'] != null}">
							<h:outputText value="Fyrsta skref, Kennitala" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['energyConsumerPersonalId']}" action="firstWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['energyConsumerHomePhone'] != null}">
							<h:outputText value="Fyrsta skref, Heimasími" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['energyConsumerHomePhone']}" action="firstWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['energyConsumerWorkPhone'] != null}">
							<h:outputText value="Fyrsta skref, Vinnusími" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['energyConsumerWorkPhone']}" action="firstWizardPage" />
						</x:div>

						<x:div rendered="#{TilkynningVertakaBean.invalid['type'] != null}">
							<h:outputText value="Annað skref, Notkunarflokkur" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['type']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['currentLineModification'] != null}">
							<h:outputText value="Annað skref, Heimtaug" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['currentLineModification']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['currentLineConnectionModification'] != null}">
							<h:outputText value="Annað skref, Heimtaug tengist i" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['currentLineConnectionModification']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['homeLine'] != null}">
							<h:outputText value="Annað skref, Stofn/kvisl" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['homeLine']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['switchPanelModification'] != null}">
							<h:outputText value="Annað skref, Aðaltafla/Mælitafla" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['switchPanelModification']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['electronicProtectiveMeasures'] != null}">
							<h:outputText value="Annað skref, Varnarráðstöfun" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['electronicProtectiveMeasures']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['application'] != null}">
							<h:outputText value="Annað skref, Beiðni um" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['application']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['power'] != null}">
							<h:outputText value="Annað skref, Uppsett afl" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['power']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['placeMeter'] != null}">
							<h:outputText value="Annað skref, Staður mælis" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['placeMeter']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['switchPanelNumber'] != null}">
							<h:outputText value="Annað skref, Númer töflu" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['switchPanelNumber']}" action="secondWizardPage" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['voltageSystemGroup'] != null}">
							<h:outputText value="Annað skref, Upplýsingar um spennukerfi" />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['voltageSystemGroup']}" action="secondWizardPage" />
						</x:div>

						<x:div rendered="#{TilkynningVertakaBean.invalid['taka'] != null}">
							<h:outputText value="Þriðja skref, Taka mæli..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['taka']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['fyrir'] != null}">
							<h:outputText value="Þriðja skref, Fyrir er..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['fyrir']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['setja'] != null}">
							<h:outputText value="Þriðja skref, Setja mæli..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['setja']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['fluttA'] != null}">
							<h:outputText value="Þriðja skref, Flutt á..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['fluttA']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['fluttAf'] != null}">
							<h:outputText value="Þriðja skref, Flutt af..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['fluttAf']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['hjalpataeki'] != null}">
							<h:outputText value="Þriðja skref, Setja hjálpatæki..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['hjalpataeki']}" action="" />
						</x:div>
						<x:div rendered="#{TilkynningVertakaBean.invalid['straumspenna'] != null}">
							<h:outputText value="Þriðja skref, Setja straumspennamæli..." />
							<h:outputText value=" " />
							<h:commandLink value="#{TilkynningVertakaBean.invalid['straumspenna']}" action="" />
						</x:div>
					</x:div>

					<f:verbatim>
						<h1 class="applicationHeading">Þjónustubeiðni</h1>
					</f:verbatim>

					<x:div styleClass="header">

						<f:verbatim>
							<h1>3. Upplýsingar um neysluveitu</h1>
						</f:verbatim>

						<!-- phases -->
						<x:div styleClass="phases">

							<f:verbatim>
								<!-- ul -->
								<ul>
									<li>1</li>
									<li>2</li>
									<li class="current">3</li>
								</ul>
								<!-- end of ul -->
							</f:verbatim>

						</x:div>
						<!-- end of phases -->

					</x:div>
					<!-- end of header -->


					<!-- form section -->
					<x:div styleClass="info">

						<x:div styleClass="personInfo" id="name" forceId="true">
							<h:outputText value="#{TilkynningLokVerksBean.rafverktaka.rafverktaki.nafn}" />
						</x:div>

						<x:div styleClass="personInfo" id="personalID" forceId="true">
							<h:outputText value="#{TilkynningLokVerksBean.rafverktaka.rafverktaki.kennitala}" />
						</x:div>

						<x:div styleClass="personInfo" id="address" forceId="true">
							<h:outputText value="#{TilkynningLokVerksBean.rafverktaka.rafverktaki.heimilisfang.display}" />
						</x:div>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader topSubHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- taka -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['taka'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['taka']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['taka'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="takaAddAnchor" name="takaAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel id="takaAddAnchor" value="Taka mæli..." />
						</x:div>

						<h:dataTable styleClass="rafverkTable" id='taka' value="#{TilkynningVertakaBean.list['taka']}" var="maelir">
							<h:column>
								<h:commandLink styleClass="addLink" id="takaAdd" action="#{maelir.add}" rendered="#{(! maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="takaAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="takaDelete" action="#{maelir.delete}" rendered="#{(maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="takaAddAnchor" />
									<f:verbatim>
										<span>Fjarlægja</span>
									</f:verbatim>
								</h:commandLink>
								<h:outputText value=" " />

								<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="taka" value="#{maelir.numer}" rendered="#{maelir.valid}" />
								<h:outputLabel for="taka" value="mæli númer" rendered="#{maelir.valid}" />
								<h:message for="taka"></h:message>
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- fyrir -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['fyrir'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['fyrir']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['fyrir'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="fyrirAddAnchor" name="fyrirAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel style="" value="Fyrir er..." />
						</x:div>
						<h:dataTable styleClass="rafverkTable" value="#{TilkynningVertakaBean.list['fyrir']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="fyrirAdd" action="#{maelir.add}" rendered="#{(! maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fyrirAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="fyrirDelete" action="#{maelir.delete}" rendered="#{(maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fyrirAddAnchor" />
									<f:verbatim>
										<span>Fjarlægja</span>
									</f:verbatim>
								</h:commandLink>

								<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="fyrir" value="#{maelir.numer}" rendered="#{maelir.valid}" />
								<h:outputLabel for="fyrir" value="mæli númer" rendered="#{maelir.valid}" />
								<h:message for="fyrir"></h:message>
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- setja maeli -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['setja'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['setja']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['setja'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="setjaAddAnchor" name="setjaAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel value="Setja mæli..." />
						</x:div>

						<h:dataTable styleClass="rafverkTable threeRowsTable" value="#{TilkynningVertakaBean.list['setja']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="setjaAdd" action="#{maelir.add}" rendered="#{(! maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="setjaAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="setjaDelete" action="#{maelir.delete}" rendered="#{maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="setjaAddAnchor" />
									<f:verbatim>
										<span>Fjarlægja</span>
									</f:verbatim>
								</h:commandLink>

								<x:div styleClass="rafverkItem">
									<h:selectOneRadio styleClass="rafverkTableCheckboxes" disabled="#{! TilkynningVertakaBean.applicationStorable}" value="#{maelir.fasa}" rendered="#{maelir.valid}">
										<f:selectItems value="#{RafverktakaInitialdata.maeliListiSelects}" />
									</h:selectOneRadio>
								</x:div>

								<x:div styleClass="rafverkItem">
									<h:outputLabel for="setja" value="Stærð" rendered="#{maelir.valid}" />
									<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" size="4" id="setja" value="#{maelir.ampere}" rendered="#{maelir.valid}" />
									<h:outputLabel for="setja" value="A" rendered="#{maelir.valid}" />
									<h:message for="setja"></h:message>
								</x:div>

								<x:div styleClass="rafverkItem">
									<h:outputLabel for="setjaT" value="Taxti" rendered="#{maelir.valid}" />
									<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="setjaT" value="#{maelir.taxti}" rendered="#{maelir.valid}" />
								</x:div>
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- flutt a -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['fluttA'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['fluttA']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['fluttA'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="fluttAAddAnchor" name="fluttAAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel value="Flutt á..." />
						</x:div>
						<h:dataTable styleClass="rafverkTable" value="#{TilkynningVertakaBean.list['fluttA']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="fluttAAdd" action="#{maelir.add}" rendered="#{! maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fluttAAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="fluttADelete" action="#{maelir.delete}" rendered="#{maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fluttAAddAnchor" />
									<f:verbatim>
										<span>Fjarlægja</span>
									</f:verbatim>
								</h:commandLink>

								<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="fluttA" value="#{maelir.numer}" rendered="#{maelir.valid}" />
								<h:outputLabel for="fluttA" value="mæli númer" rendered="#{maelir.valid}" />
								<h:message for="fluttA"></h:message>
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- flutt af -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['fluttAf'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['fluttAf']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['fluttAf'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="fluttAfAddAnchor" name="fluttAfAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel value="Flutt af..." />
						</x:div>

						<h:dataTable styleClass="rafverkTable" value="#{TilkynningVertakaBean.list['fluttAf']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="fluttAfAdd" action="#{maelir.add}" rendered="#{! maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fluttAfAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="fluttAfDelete" action="#{maelir.delete}" rendered="#{maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="fluttAfAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="fluttAf" value="#{maelir.numer}" rendered="#{maelir.valid}" />
								<h:outputLabel for="fluttAf" value="mæli númer" rendered="#{maelir.valid}" />
								<h:message for="fluttAf"></h:message>
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- hjalpataeki -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['hjalpataeki'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['hjalpataeki']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['hjalpataeki'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="hjalpataekiAddAnchor" name="hjalpataekiAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel value="Setja hjálpatæki..." />
						</x:div>

						<h:dataTable styleClass="rafverkTable" value="#{TilkynningVertakaBean.list['hjalpataeki']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="hjalpataekiAdd" action="#{maelir.add}" rendered="#{(! maelir.valid) and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="hjalpataekiAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="hjalpataekiDelete" action="#{maelir.delete}" rendered="#{maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="hjalpataekiAddAnchor" />
								</h:commandLink>

								<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="hjalpataeki" value="#{maelir.hjalpataeki}" rendered="#{maelir.valid}" />
							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- setja maeli -->
						<x:div style="formItem" rendered="#{TilkynningVertakaBean.invalid['straumspenna'] != null}">
							<h:outputText style="color:red" value="#{TilkynningVertakaBean.invalid['straumspenna']}" />
						</x:div>
						<x:div styleClass="#{TilkynningVertakaBean.invalid['straumspenna'] != null ? 'formItem hasError' : 'formItem'}">
							<f:verbatim>
								<a id="straumspennaAddAnchor" name="straumspennaAddAnchor" style="color: white">&#160;</a>
							</f:verbatim>
							<h:outputLabel value="Setja straumspennamæli..." />
						</x:div>

						<h:dataTable styleClass="rafverkTable twoRowsTable" value="#{TilkynningVertakaBean.list['straumspenna']}" var="maelir">
							<h:column>

								<h:commandLink styleClass="addLink" id="straumspennaAdd" action="#{maelir.add}" rendered="#{! maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="straumspennaAddAnchor" />
									<f:verbatim>
										<span>Bæta við</span>
									</f:verbatim>
								</h:commandLink>

								<h:commandLink styleClass="removeLink" id="straumspennaDelete" action="#{maelir.delete}" rendered="#{maelir.valid and (TilkynningVertakaBean.applicationStorable)}">
									<f:param name="anchorName" value="straumspennaAddAnchor" />
									<f:verbatim>
										<span>Fjarlægja</span>
									</f:verbatim>
								</h:commandLink>

								<x:div styleClass="rafverkItem">
									<h:outputLabel for="straumspenna" value="Stærð" rendered="#{maelir.valid}" />
									<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" size="4" id="straumspenna" value="#{maelir.ampere}" rendered="#{maelir.valid}" />
									<h:outputLabel for="straumspenna" value="A" rendered="#{maelir.valid}" />
									<h:message for="straumspenna"></h:message>
								</x:div>

								<x:div styleClass="rafverkItem">
									<h:outputLabel for="straumspennaT" value="Taxti" rendered="#{maelir.valid}" />
									<h:inputText disabled="#{! TilkynningVertakaBean.applicationStorable}" id="straumspennaT" value="#{maelir.taxti}" rendered="#{maelir.valid}" />
								</x:div>

							</h:column>
						</h:dataTable>

					</x:div>
					<!-- end of formsection-->

					<f:verbatim>
						<h1 class="subHeader">Tilkynning um rafverktöku</h1>
					</f:verbatim>

					<!-- form section -->
					<x:div styleClass="formSection">

						<!-- 26 -->
						<x:div styleClass="formItem">
							<h:outputLabel for="skyringar" value="Skýringar" />
							<h:inputTextarea disabled="#{! TilkynningVertakaBean.applicationStorable}" id="skyringar" rows="3" cols="60" value="#{TilkynningVertakaBean.skyringar}" />
						</x:div>

					</x:div>
					<!-- end of formsection-->

					<x:div styleClass="bottom">
						<h:commandLink styleClass="button" actionListener="#{TilkynningVertakaBean.triggerValidation}" action="back">
							<f:verbatim>
								<span class="buttonSpan">Til baka</span>
							</f:verbatim>
						</h:commandLink>

						<h:commandLink styleClass="button" rendered="#{TilkynningVertakaBean.applicationSendable}" action="#{TilkynningVertakaBean.send}">
							<f:verbatim>
								<span class="buttonSpan">Senda</span>
							</f:verbatim>
						</h:commandLink>

						<h:commandLink styleClass="button" id="storeButton" rendered="#{TilkynningVertakaBean.applicationStorable}" action="#{TilkynningVertakaBean.store}">
							<f:verbatim>
								<span class="buttonSpan">Geyma sem uppkast</span>
							</f:verbatim>
						</h:commandLink>
					</x:div>
				</h:form>
			</builder:region>
		</builder:page>
	</f:view>
</jsp:root>