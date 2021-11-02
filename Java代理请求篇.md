```
   @Test
    public void httpTest() throws IOException {
      /*  String ss = HttpUtils.sendGet("https://api.pancakeswap.info/api/v2/tokens");

        System.out.println(ss);*/
        String url = "https://api.pancakeswap.info/api/v2/tokens";
      /*  HttpGet httpGet = new HttpGet("https://api.pancakeswap.info/api/v2/tokens");*/

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("101.32.239.132", 50297),
                new UsernamePasswordCredentials("TKKNGqIPf7", "uWZu69UXAa"));

        CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        HttpHost proxy = new HttpHost("101.32.239.132",50297 );
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
