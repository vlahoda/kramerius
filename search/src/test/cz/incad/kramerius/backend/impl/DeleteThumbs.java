package cz.incad.kramerius.backend.impl;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;

import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.LoggingHandler;

public class DeleteThumbs {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DeleteThumbs.class.getName());
	
	public static String []ARRAY = {
    "uuid:814a6eb0-934c-11de-a77b-000d606f5dc6",
    "uuid:17abff10-96f7-11de-9c16-000d606f5dc6",
    "uuid:814d06c0-934c-11de-b0e0-000d606f5dc6",
    "uuid:17b01dc0-96f7-11de-9176-000d606f5dc6",
    "uuid:81501400-934c-11de-93e0-000d606f5dc6",
    "uuid:17b5c310-96f7-11de-86f4-000d606f5dc6",
    "uuid:8152ac10-934c-11de-b804-000d606f5dc6",
    "uuid:17ba56f0-96f7-11de-a20b-000d606f5dc6",
    "uuid:81551d10-934c-11de-97ed-000d606f5dc6",
    "uuid:17be4e90-96f7-11de-bfe8-000d606f5dc6",
    "uuid:815bd3d0-934c-11de-8f0f-000d606f5dc6",
    "uuid:17c26d40-96f7-11de-bcc2-000d606f5dc6",
    "uuid:815dcfa0-934c-11de-8358-000d606f5dc6",
    "uuid:17c70120-96f7-11de-b854-000d606f5dc6",
    "uuid:816374f0-934c-11de-a3d8-000d606f5dc6",
    "uuid:17cb9500-96f7-11de-8377-000d606f5dc6",
    "uuid:8165e5f0-934c-11de-8629-000d606f5dc6",
    "uuid:17cf1770-96f7-11de-aa78-000d606f5dc6",
    "uuid:81687e00-934c-11de-9804-000d606f5dc6",
    "uuid:17d33620-96f7-11de-8d6b-000d606f5dc6",
    "uuid:816b8b40-934c-11de-b5b6-000d606f5dc6",
    "uuid:17d83f30-96f7-11de-a557-000d606f5dc6",
    "uuid:81709450-934c-11de-a777-000d606f5dc6",
    "uuid:17dc5de0-96f7-11de-a9e4-000d606f5dc6",
    "uuid:8172b730-934c-11de-9eb4-000d606f5dc6",
    "uuid:17e166f0-96f7-11de-bc94-000d606f5dc6",
    "uuid:8174b300-934c-11de-bc24-000d606f5dc6",
    "uuid:17e892e0-96f7-11de-bd59-000d606f5dc6",
    "uuid:81774b10-934c-11de-a2ac-000d606f5dc6",
    "uuid:17ed26c0-96f7-11de-947a-000d606f5dc6",
    "uuid:8179bc10-934c-11de-ad31-000d606f5dc6",
    "uuid:17f0a930-96f7-11de-b113-000d606f5dc6",
    "uuid:817c5420-934c-11de-a781-000d606f5dc6",
    "uuid:17f53d10-96f7-11de-ae41-000d606f5dc6",
    "uuid:8182e3d0-934c-11de-bc7f-000d606f5dc6",
    "uuid:17f95bc0-96f7-11de-a9e6-000d606f5dc6",
    "uuid:81857be0-934c-11de-bfe2-000d606f5dc6",
    "uuid:17ff7640-96f7-11de-b620-000d606f5dc6",
    "uuid:81888920-934c-11de-9922-000d606f5dc6",
    "uuid:1809af70-96f7-11de-a803-000d606f5dc6",
    "uuid:818d9230-934c-11de-b54a-000d606f5dc6",
    "uuid:180da710-96f7-11de-a8a6-000d606f5dc6",
    "uuid:818fb510-934c-11de-95c8-000d606f5dc6",
    "uuid:18123af0-96f7-11de-917b-000d606f5dc6",
    "uuid:8191b0e0-934c-11de-9f70-000d606f5dc6",
    "uuid:181659a0-96f7-11de-ae10-000d606f5dc6",
    "uuid:819644c0-934c-11de-8f94-000d606f5dc6",
    "uuid:181aed80-96f7-11de-829d-000d606f5dc6",
    "uuid:81984090-934c-11de-a346-000d606f5dc6",
    "uuid:181ff690-96f7-11de-be10-000d606f5dc6",
    "uuid:819ad8a0-934c-11de-8e1c-000d606f5dc6",
    "uuid:18241540-96f7-11de-8eb7-000d606f5dc6",
    "uuid:819d70b0-934c-11de-b74e-000d606f5dc6",
    "uuid:182833f0-96f7-11de-accf-000d606f5dc6",
    "uuid:81a07df0-934c-11de-8c8f-000d606f5dc6",
    "uuid:182c2b90-96f7-11de-be69-000d606f5dc6",
    "uuid:81a2eef0-934c-11de-9809-000d606f5dc6",
    "uuid:1831d0e0-96f7-11de-8f9f-000d606f5dc6",
    "uuid:81a782d0-934c-11de-8bb0-000d606f5dc6",
    "uuid:18355350-96f7-11de-98a1-000d606f5dc6",
    "uuid:81ac16b0-934c-11de-921b-000d606f5dc6",
    "uuid:183c0a10-96f7-11de-a54e-000d606f5dc6",
    "uuid:81aeaec0-934c-11de-a8b4-000d606f5dc6",
    "uuid:18411320-96f7-11de-81cf-000d606f5dc6",
    "uuid:81b0aa90-934c-11de-a162-000d606f5dc6",
    "uuid:184531d0-96f7-11de-96e5-000d606f5dc6",
    "uuid:81b342a0-934c-11de-a051-000d606f5dc6",
    "uuid:18492970-96f7-11de-be3e-000d606f5dc6",
    "uuid:81b53e70-934c-11de-8519-000d606f5dc6",
    "uuid:184e5990-96f7-11de-a8ad-000d606f5dc6",
    "uuid:81b95d20-934c-11de-9f00-000d606f5dc6",
    "uuid:18525130-96f7-11de-87b5-000d606f5dc6",
    "uuid:81bd7bd0-934c-11de-9152-000d606f5dc6",
    "uuid:18586bb0-96f7-11de-be02-000d606f5dc6",
    "uuid:81c20fb0-934c-11de-ae53-000d606f5dc6",
    "uuid:185c8a60-96f7-11de-a1ba-000d606f5dc6",
    "uuid:81c480b0-934c-11de-882f-000d606f5dc6",
    "uuid:18622fb0-96f7-11de-8695-000d606f5dc6",
    "uuid:81c718c0-934c-11de-baee-000d606f5dc6",
    "uuid:1866c390-96f7-11de-88db-000d606f5dc6",
    "uuid:81ceb9e0-934c-11de-8319-000d606f5dc6",
    "uuid:186ed9e0-96f7-11de-9f9c-000d606f5dc6",
    "uuid:81d0b5b0-934c-11de-a107-000d606f5dc6",
    "uuid:1872f890-96f7-11de-a8f9-000d606f5dc6",
    "uuid:81d34dc0-934c-11de-b316-000d606f5dc6",
    "uuid:1876f030-96f7-11de-b328-000d606f5dc6",
    "uuid:81d54990-934c-11de-9644-000d606f5dc6",
    "uuid:187c9580-96f7-11de-abb7-000d606f5dc6",
    "uuid:81d8f310-934c-11de-9c1c-000d606f5dc6",
    "uuid:1882b000-96f7-11de-867e-000d606f5dc6",
    "uuid:81dc7580-934c-11de-a275-000d606f5dc6",
    "uuid:1887b910-96f7-11de-a9f9-000d606f5dc6",
    "uuid:81de7150-934c-11de-881d-000d606f5dc6",
    "uuid:188bd7c0-96f7-11de-849f-000d606f5dc6",
    "uuid:81e10960-934c-11de-8f80-000d606f5dc6",
    "uuid:1890e0d0-96f7-11de-8f3c-000d606f5dc6",
    "uuid:81e61270-934c-11de-bdd5-000d606f5dc6",
    "uuid:189b1a00-96f7-11de-b45b-000d606f5dc6",
    "uuid:81e83550-934c-11de-8e35-000d606f5dc6",
    "uuid:189fade0-96f7-11de-a614-000d606f5dc6",
    "uuid:81ea3120-934c-11de-8a47-000d606f5dc6",
    "uuid:18a441c0-96f7-11de-8754-000d606f5dc6",
    "uuid:81ecc930-934c-11de-a9b3-000d606f5dc6",
    "uuid:18aad170-96f7-11de-aebb-000d606f5dc6",
    "uuid:81ef3a30-934c-11de-b6e2-000d606f5dc6",
    "uuid:18b18830-96f7-11de-a448-000d606f5dc6",
    "uuid:81f2e3b0-934c-11de-bc94-000d606f5dc6",
    "uuid:18b61c10-96f7-11de-ae2a-000d606f5dc6",
    "uuid:81f4df80-934c-11de-8b8f-000d606f5dc6",
    "uuid:18ba13b0-96f7-11de-a1a4-000d606f5dc6",
    "uuid:81f7ecc0-934c-11de-88d3-000d606f5dc6",
    "uuid:18bea790-96f7-11de-8184-000d606f5dc6",
    "uuid:81fafa00-934c-11de-b171-000d606f5dc6",
    "uuid:18c2c640-96f7-11de-a442-000d606f5dc6",
    "uuid:81ff18b0-934c-11de-b2d7-000d606f5dc6",
    "uuid:18c8e0c0-96f7-11de-9726-000d606f5dc6",
    "uuid:820189b0-934c-11de-b992-000d606f5dc6",
    "uuid:18cefb40-96f7-11de-9d87-000d606f5dc6",
    "uuid:820421c0-934c-11de-9394-000d606f5dc6",
    "uuid:18d58af0-96f7-11de-8a74-000d606f5dc6",
    "uuid:82061d90-934c-11de-a18d-000d606f5dc6",
    "uuid:18db3040-96f7-11de-9cb9-000d606f5dc6",
    "uuid:8208b5a0-934c-11de-93be-000d606f5dc6",
    "uuid:18e14ac0-96f7-11de-9a01-000d606f5dc6",
    "uuid:820a3c40-934c-11de-8099-000d606f5dc6",
    "uuid:18e9fd50-96f7-11de-b1de-000d606f5dc6",
    "uuid:820f4550-934c-11de-8e3e-000d606f5dc6",
    "uuid:18ef0660-96f7-11de-9a7e-000d606f5dc6",
    "uuid:82125290-934c-11de-9973-000d606f5dc6",
    "uuid:18f93f90-96f7-11de-bbb2-000d606f5dc6",
    "uuid:82147570-934c-11de-9539-000d606f5dc6",
    "uuid:18fd3730-96f7-11de-b7c7-000d606f5dc6",
    "uuid:82167140-934c-11de-ac76-000d606f5dc6",
    "uuid:1902dc80-96f7-11de-8cd2-000d606f5dc6",
    "uuid:82186d10-934c-11de-9cd6-000d606f5dc6",
    "uuid:19077060-96f7-11de-b5b2-000d606f5dc6",
    "uuid:821b0520-934c-11de-855a-000d606f5dc6",
    "uuid:190b8f10-96f7-11de-913e-000d606f5dc6",
    "uuid:821d00f0-934c-11de-ae3c-000d606f5dc6",
    "uuid:191022f0-96f7-11de-a23d-000d606f5dc6",
    "uuid:8222a640-934c-11de-8118-000d606f5dc6",
    "uuid:19152c00-96f7-11de-97d5-000d606f5dc6",
    "uuid:82273a20-934c-11de-ac52-000d606f5dc6",
    "uuid:1918ae70-96f7-11de-b073-000d606f5dc6",
    "uuid:822e6610-934c-11de-8373-000d606f5dc6",
    "uuid:191fda60-96f7-11de-a490-000d606f5dc6",
    "uuid:82336f20-934c-11de-af27-000d606f5dc6",
    "uuid:1923f910-96f7-11de-8c28-000d606f5dc6",
    "uuid:82356af0-934c-11de-b9ac-000d606f5dc6",
    "uuid:19290220-96f7-11de-b216-000d606f5dc6",
    "uuid:82380300-934c-11de-b004-000d606f5dc6",
    "uuid:192f1ca0-96f7-11de-932e-000d606f5dc6",
    "uuid:8239fed0-934c-11de-b190-000d606f5dc6",
    "uuid:1933b080-96f7-11de-81f4-000d606f5dc6",
    "uuid:823c21b0-934c-11de-be47-000d606f5dc6",
    "uuid:1938b990-96f7-11de-97e3-000d606f5dc6",
    "uuid:823fa420-934c-11de-a7ef-000d606f5dc6",
    "uuid:193e5ee0-96f7-11de-83b0-000d606f5dc6",
    "uuid:82443800-934c-11de-a393-000d606f5dc6",
    "uuid:1942f2c0-96f7-11de-a0ae-000d606f5dc6",
    "uuid:8246d010-934c-11de-8cfe-000d606f5dc6",
    "uuid:19498270-96f7-11de-84b1-000d606f5dc6",
    "uuid:824bd920-934c-11de-8432-000d606f5dc6",
    "uuid:194da120-96f7-11de-aa7d-000d606f5dc6",
    "uuid:824f5b90-934c-11de-b885-000d606f5dc6",
    "uuid:19523500-96f7-11de-8697-000d606f5dc6",
    "uuid:825268d0-934c-11de-9091-000d606f5dc6",
    "uuid:19584f80-96f7-11de-8af5-000d606f5dc6",
    "uuid:825500e0-934c-11de-9898-000d606f5dc6",
    "uuid:195e6a00-96f7-11de-a43f-000d606f5dc6",
    "uuid:8256fcb0-934c-11de-ad68-000d606f5dc6",
    "uuid:1962fde0-96f7-11de-8ac9-000d606f5dc6",
    "uuid:82591f90-934c-11de-9266-000d606f5dc6",
    "uuid:196806f0-96f7-11de-8281-000d606f5dc6",
    "uuid:8262bc80-934c-11de-849d-000d606f5dc6",
    "uuid:196d3710-96f7-11de-9093-000d606f5dc6",
    "uuid:82655490-934c-11de-a49e-000d606f5dc6",
    "uuid:1972b550-96f7-11de-be8e-000d606f5dc6",
    "uuid:82675060-934c-11de-973a-000d606f5dc6",
    "uuid:1976d400-96f7-11de-b7cc-000d606f5dc6",
    "uuid:826cf5b0-934c-11de-af94-000d606f5dc6",
    "uuid:197b67e0-96f7-11de-803c-000d606f5dc6",
    "uuid:82738560-934c-11de-ad17-000d606f5dc6",
    "uuid:197f8690-96f7-11de-ab58-000d606f5dc6",
    "uuid:82761d70-934c-11de-89c4-000d606f5dc6",
    "uuid:19841a70-96f7-11de-88f0-000d606f5dc6",
    "uuid:82792ab0-934c-11de-be72-000d606f5dc6",
    "uuid:198a34f0-96f7-11de-8fb3-000d606f5dc6",
    "uuid:827c37f0-934c-11de-ba04-000d606f5dc6",
    "uuid:198f3e00-96f7-11de-8cee-000d606f5dc6",
    "uuid:827f4530-934c-11de-9c5a-000d606f5dc6",
    "uuid:1993d1e0-96f7-11de-aec8-000d606f5dc6",
    "uuid:8281b630-934c-11de-bd0b-000d606f5dc6",
    "uuid:1998daf0-96f7-11de-aa63-000d606f5dc6",
    "uuid:82844e40-934c-11de-8af0-000d606f5dc6",
    "uuid:199c8470-96f7-11de-bd1f-000d606f5dc6",
    "uuid:8286e650-934c-11de-814a-000d606f5dc6",
    "uuid:19a006e0-96f7-11de-a754-000d606f5dc6",
    "uuid:8288e220-934c-11de-b68e-000d606f5dc6",
    "uuid:19a50ff0-96f7-11de-99d5-000d606f5dc6",
    "uuid:828b7a30-934c-11de-9078-000d606f5dc6",
    "uuid:19a9a3d0-96f7-11de-8cd4-000d606f5dc6",
    "uuid:828d7600-934c-11de-bb64-000d606f5dc6",
    "uuid:19af4920-96f7-11de-add3-000d606f5dc6",
    "uuid:82927f10-934c-11de-ae9f-000d606f5dc6",
    "uuid:19b367d0-96f7-11de-bcf8-000d606f5dc6",
    "uuid:82958c50-934c-11de-9d9e-000d606f5dc6",
    "uuid:19b6ea40-96f7-11de-9e38-000d606f5dc6",
    "uuid:82989990-934c-11de-b377-000d606f5dc6",
    "uuid:19bbf350-96f7-11de-aadc-000d606f5dc6",
    "uuid:829b31a0-934c-11de-be89-000d606f5dc6",
    "uuid:19c01200-96f7-11de-9d37-000d606f5dc6",
    "uuid:829dc9b0-934c-11de-a03a-000d606f5dc6",
    "uuid:19c51b10-96f7-11de-b75b-000d606f5dc6",
    "uuid:82a0d6f0-934c-11de-bf27-000d606f5dc6",
    "uuid:19cac060-96f7-11de-ad6b-000d606f5dc6",
    "uuid:82a347f0-934c-11de-8ee6-000d606f5dc6",
    "uuid:19d065b0-96f7-11de-b0a2-000d606f5dc6",
    "uuid:82a8ed40-934c-11de-aeb6-000d606f5dc6",
    "uuid:19d5e3f0-96f7-11de-8630-000d606f5dc6",
    "uuid:82ab8550-934c-11de-b840-000d606f5dc6",
    "uuid:19db1410-96f7-11de-8f08-000d606f5dc6",
    "uuid:82adf650-934c-11de-9d97-000d606f5dc6",
    "uuid:19dfa7f0-96f7-11de-bed4-000d606f5dc6",
    "uuid:82b08e60-934c-11de-8c75-000d606f5dc6",
    "uuid:19e39f90-96f7-11de-91d0-000d606f5dc6",
    "uuid:82b32670-934c-11de-9891-000d606f5dc6",
    "uuid:19e7be40-96f7-11de-8a9f-000d606f5dc6",
    "uuid:82b52240-934c-11de-b8e8-000d606f5dc6",
    "uuid:19ecc750-96f7-11de-99d5-000d606f5dc6",
    "uuid:82b7ba50-934c-11de-b5df-000d606f5dc6",
    "uuid:19f070d0-96f7-11de-8b4e-000d606f5dc6",
    "uuid:82bb3cc0-934c-11de-a6d1-000d606f5dc6",
    "uuid:19f3f340-96f7-11de-a8c8-000d606f5dc6",
    "uuid:82bdd4d0-934c-11de-96aa-000d606f5dc6",
    "uuid:19f88720-96f7-11de-a307-000d606f5dc6",
    "uuid:82c15740-934c-11de-b15e-000d606f5dc6",
    "uuid:19fca5d0-96f7-11de-9d9b-000d606f5dc6",
    "uuid:82c46480-934c-11de-b741-000d606f5dc6",
    "uuid:1a022410-96f7-11de-a895-000d606f5dc6",
    "uuid:82c771c0-934c-11de-8065-000d606f5dc6",
    "uuid:1a075430-96f7-11de-809f-000d606f5dc6",
    "uuid:82cd1710-934c-11de-853d-000d606f5dc6",
    "uuid:1a0ad6a0-96f7-11de-9c2f-000d606f5dc6",
    "uuid:82cf12e0-934c-11de-b991-000d606f5dc6",
    "uuid:1a0e5910-96f7-11de-95b3-000d606f5dc6",
    "uuid:82d22020-934c-11de-94c7-000d606f5dc6",
    "uuid:1a1277c0-96f7-11de-a176-000d606f5dc6",
    "uuid:82d4b830-934c-11de-b4e6-000d606f5dc6",
    "uuid:1a169670-96f7-11de-abd6-000d606f5dc6",
    "uuid:82d72930-934c-11de-b18c-000d606f5dc6",
    "uuid:1a1a8e10-96f7-11de-824e-000d606f5dc6",
    "uuid:82da3670-934c-11de-adc9-000d606f5dc6",
    "uuid:1a1f21f0-96f7-11de-a529-000d606f5dc6",
    "uuid:82dc5950-934c-11de-a1a1-000d606f5dc6",
    "uuid:1a23b5d0-96f7-11de-9de4-000d606f5dc6",
    "uuid:82e46fa0-934c-11de-a87f-000d606f5dc6",
    "uuid:1a28e5f0-96f7-11de-923f-000d606f5dc6",
    "uuid:82e77ce0-934c-11de-8e15-000d606f5dc6",
    "uuid:1a2c6860-96f7-11de-ba11-000d606f5dc6",
    "uuid:82ea14f0-934c-11de-b8a7-000d606f5dc6",
    "uuid:1a30fc40-96f7-11de-b026-000d606f5dc6",
    "uuid:82ed2230-934c-11de-a8d8-000d606f5dc6",
    "uuid:1a359020-96f7-11de-b973-000d606f5dc6",
    "uuid:82ef9330-934c-11de-9adf-000d606f5dc6",
    "uuid:1a391290-96f7-11de-a856-000d606f5dc6",
    "uuid:82f73450-934c-11de-a7ae-000d606f5dc6",
    "uuid:1a3eb7e0-96f7-11de-a7e6-000d606f5dc6",
    "uuid:82f95730-934c-11de-a596-000d606f5dc6",
    "uuid:1a42d690-96f7-11de-869e-000d606f5dc6",
    "uuid:82fcd9a0-934c-11de-8a6c-000d606f5dc6",
    "uuid:1a46ce30-96f7-11de-be00-000d606f5dc6",
    "uuid:82ffe6e0-934c-11de-84cc-000d606f5dc6",
    "uuid:1a4b6210-96f7-11de-b324-000d606f5dc6",
    "uuid:8301e2b0-934c-11de-a31c-000d606f5dc6",
    "uuid:1a4ff5f0-96f7-11de-b7c4-000d606f5dc6",
    "uuid:83047ac0-934c-11de-a6cb-000d606f5dc6",
    "uuid:1a5414a0-96f7-11de-a8c1-000d606f5dc6",
    "uuid:8307fd30-934c-11de-9749-000d606f5dc6",
    "uuid:1a579710-96f7-11de-a157-000d606f5dc6",
    "uuid:830a9540-934c-11de-ac2d-000d606f5dc6",
    "uuid:1a5cc730-96f7-11de-8bd9-000d606f5dc6",
    "uuid:830c9110-934c-11de-bfa4-000d606f5dc6",
    "uuid:1a61d040-96f7-11de-b7b6-000d606f5dc6",
    "uuid:830f2920-934c-11de-bd6c-000d606f5dc6",
    "uuid:1a677590-96f7-11de-94b0-000d606f5dc6",
    "uuid:8311c130-934c-11de-9a22-000d606f5dc6",
    "uuid:1a6c0970-96f7-11de-8dd9-000d606f5dc6",
    "uuid:83143230-934c-11de-9ccd-000d606f5dc6",
    "uuid:1a709d50-96f7-11de-aa9d-000d606f5dc6",
    "uuid:8316ca40-934c-11de-8064-000d606f5dc6",
    "uuid:1a753130-96f7-11de-914e-000d606f5dc6",
    "uuid:83196250-934c-11de-990b-000d606f5dc6",
    "uuid:1a79c510-96f7-11de-bb31-000d606f5dc6",
    "uuid:831bd350-934c-11de-94b6-000d606f5dc6",
    "uuid:1a7dbcb0-96f7-11de-8499-000d606f5dc6",
    "uuid:831df630-934c-11de-82f1-000d606f5dc6",
    "uuid:1a847370-96f7-11de-b472-000d606f5dc6",
    "uuid:831ff200-934c-11de-802a-000d606f5dc6",
    "uuid:1a897c80-96f7-11de-87f6-000d606f5dc6",
    "uuid:8321edd0-934c-11de-a95f-000d606f5dc6",
    "uuid:1a8e1060-96f7-11de-a281-000d606f5dc6",
    "uuid:832485e0-934c-11de-baae-000d606f5dc6",
    "uuid:1a93b5b0-96f7-11de-af28-000d606f5dc6",
    "uuid:832919c0-934c-11de-8c0e-000d606f5dc6",
    "uuid:1a97ad50-96f7-11de-a4fe-000d606f5dc6",
    "uuid:832b1590-934c-11de-9254-000d606f5dc6",
    "uuid:1a9bcc00-96f7-11de-9001-000d606f5dc6",
    "uuid:832dada0-934c-11de-97a7-000d606f5dc6",
    "uuid:1aa05fe0-96f7-11de-961b-000d606f5dc6",
    "uuid:832fa970-934c-11de-bccb-000d606f5dc6",
    "uuid:1aa3e250-96f7-11de-933f-000d606f5dc6"};

	public static void main(String[] args) {
		
		
		FedoraAPIMService service = null;
		FedoraAPIM port = null;
		Authenticator.setDefault(new Authenticator() { 
	        protected PasswordAuthentication getPasswordAuthentication() { 
	           return new PasswordAuthentication("fedoraAdmin", "fedoraAdmin".toCharArray()); 
	         }
        });
	
        String spec = "http://194.108.215.227:8080/fedora"+"/wsdl?api=API-M";
		LOGGER.info("API-M"+spec);
	    try {
			service = new FedoraAPIMService(new URL(spec),
	                new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-M-Service"));
	    } catch (MalformedURLException e) {
	        System.out.println(e);
	        e.printStackTrace();
	    }
	    port = service.getPort(FedoraAPIM.class);
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "fedoraAdmin");
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "fedoraAdmin");
	    Binding binding = ((BindingProvider) port).getBinding();
	    List<Handler> chain = binding.getHandlerChain();
	    if (chain == null) {
	    	chain = new ArrayList<Handler>();
	    }
    	chain.add(new LoggingHandler());
	    LOGGER.info("adding logger to chain ");
	    binding.setHandlerChain(chain);
	    
	    for (String pid : ARRAY) {
		    List<String> purgeDatastream = port.purgeDatastream(pid, FedoraUtils.IMG_THUMB, null, null, "", false);
		    System.out.println(purgeDatastream);
			
		}
	}
}
