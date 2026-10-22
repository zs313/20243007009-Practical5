# Municipality text and website sources

The four original English summaries in `app/src/main/res/values/arrays.xml` paraphrase factual information from the official sources below. They are intended as short educational introductions, not quotations. Array order follows the explicit written requirement: **Beijing, Shanghai, Chongqing, Tianjin**.

Sources checked on **22 October 2026**. No changing population, economic or visitor statistics are used.

## Beijing

- [Facts, Beijing municipal government](https://english.beijing.gov.cn/beijinginfo/abstract/202005/t20200512_1895468.html): capital status and national functions.
- [Must-visit Sites, Beijing municipal government](https://english.beijing.gov.cn/travellinginbeijing/mustvisitsites/): Palace Museum, Summer Palace, Temple of Heaven and Badaling Great Wall.
- [Beijing municipal government overview](https://www.beijing.gov.cn/renwen/bjgk/): geography and government status.
- Visit site destination: [Beijing municipal government English portal](https://english.beijing.gov.cn/).

## Shanghai

- [Overview, Shanghai municipal government](https://english.shanghai.gov.cn/en-Overview/index.html): municipality, Yangtze estuary, Delta, economic roles and municipal flower.
- [Pudong city tour, Shanghai municipal government](https://english.shanghai.gov.cn/en-CityTour/20250530/b8fbbd2155c4468bab9b363a5e6c566a.html): Huangpu River, Puxi and Pudong.
- [Huangpu River tour routes, official Shanghai tourism website](https://www.meet-in-shanghai.net/en/guide/exploring-shanghais-waterfront-10-huangpu-river-tour-routes-603448/): Bund, Lujiazui, Nanjing Road, Yu Garden and historic neighbourhoods.
- Visit site destination: [Shanghai municipal government English portal](https://english.shanghai.gov.cn/).

## Chongqing

- [Geography, Chongqing municipal government](https://english.cq.gov.cn/aboutchongqing/overview/Geography/): upper Yangtze, river confluence, mountain landscape, karst features, urban/rural extent and regional functions.
- [Chongqing municipal government English portal](https://english.cq.gov.cn/index.html): municipality status and industry/transport context.
- Visit site destination: [Chongqing municipal government English portal](https://english.cq.gov.cn/).
- Verification limitation: official pages and current content were discoverable in the search index, but direct retrieval repeatedly timed out in the research web tool. This is a reachability limitation of this check, not proof that the URL is invalid. The alternative [iChongqing portal](https://www.ichongqing.info/) fetched successfully and is operated by the Western China International Communication Organization; it is not the municipal government portal.

## Tianjin

- [Tianjin transformation overview, official Tianjin portal](https://www.exploringtianjin.com/2026-04/21/c_1177791.htm): Haihe, Grand Canal, maritime gateway, port, transport, Tianjin Eye and architectural heritage.
- [Location Advantage, official Tianjin portal](https://www.exploringtianjin.com/2023-07/13/c_902646.htm): coastal location and shipping connections.
- [Tianjin Guide, Tianjin municipal government information office](https://www.exploringtianjin.com/pdf/TianjinGuide.pdf): municipality and geographic context.
- Visit site destination: [Tianjin municipal government English portal](https://en.tj.gov.cn/), as suggested in the assignment. This portal was also fetched successfully during the main implementation review. The supplementary Exploring Tianjin source credits Tianjin Municipal Government and China Daily in its footer.

## Website check notes

- Shanghai and Tianjin homepages fetched successfully on the check date.
- Beijing English fact pages fetched successfully; its English homepage timed out once, while its Chinese government homepage fetched successfully.
- Test each in-app button separately using the device browser. A successful ACTION_VIEW launch verifies the Android intent; network page loading is a separate check.
