import { Configuration, Middleware, RequestContext } from './runtime';
import { DefaultApi } from './apis';

export default class ChuuApi {
    private _baseApi: DefaultApi;

    get baseApi(): DefaultApi {
      return this._baseApi;
    }

    constructor(token: string) {
      console.log('Constructing api with token', token);
      const middleware: Middleware['pre'] = (context: RequestContext) => {
        if (token && token !== 'INVALID_TOKEN') {
          context.init.headers['Authorization'] = `Bearer ${token}`;
        }
        console.log(context.init.headers);
        context.init.headers['Access-Control-Allow-Origin'] = '*';
        return Promise.resolve({ url: context.url, init: context.init });
      };
      const logResponse: Middleware['post'] = (context) => {
        console.log(context.response);
        console.log(context.url);
        return Promise.resolve();
      };
      const configuration = new Configuration({
        basePath: 'http://localhost:8080',
        middleware: [{ pre: middleware, post: logResponse }],
      });
      this._baseApi = new DefaultApi(configuration);
    }
}
