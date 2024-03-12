<p>Настройки образов + наброски приложений для игр со стримом фреймов из mp4 на OpenCV + Spring Boot</p>
<p>Настройки образов без сессий и GPU под WSL2 на PC</p>
<p/>

<p><b>В порядке запуска:</b></p>
<p><b>dotwslconfig</b> - пример настроек .wslconfig</p>
<p><b>kafka-brokers-conf</b> - пример поднятия топиков</p>
<p><b>card-image-processing-base-java-image</b> - образ со сборкой OpenCV для JVM</p>
<p><b>card-image-processing-stream-service-impl</b> - WebFlux приложение для парсинга mp4 и чтения ответа из топика (kafka topic->WebSocket)</p>
<p><b>card-object-detection-service-kafka</b> - образ с Python сервисом распознавания объектов</p>
<p><i>*предобученная на распознавание дебетовых карт FasterRCNN модель /card-object-detection-service-kafka/ocv/fasterrcnn_resnet50_fpn_card_0.pth ~150mb , github не поддерживает размер</i></p>
<p><b>card-text-detection-service-kafka</b> - образ с Python сервисом распознавания текста, EasyOCR без настроек</p>
<p><b>card-image-processing-ui</b> - React клиент</p>



| исходный фрейм        | результат             |
|-----------------------|-----------------------|
| ![](./img/source.jpg) | ![](./img/result.jpg) |