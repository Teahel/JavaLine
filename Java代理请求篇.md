```
   @Test
    public void httpTest() throws IOException {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("1.32.23.12", 0597),
                new UsernamePasswordCredentials("TKKNf7", "uWZu6a"));

        CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        HttpHost proxy = new HttpHost("1.32.23.12",0597 );
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();


        //2.生成一个get请求
        HttpGet httpget = new HttpGet(url);
        httpget.setConfig(config);
        CloseableHttpResponse response = null;
        try {
            //3.执行get请求并返回结果
            response = client.execute(httpget);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String result = null;
        try {
            //4.处理结果，这里将结果返回为字符串
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(result);
    }
```
