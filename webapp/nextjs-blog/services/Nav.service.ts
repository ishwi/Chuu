import Router from 'next/router';
import {NextPageContext} from 'next/types';

class NavService {
    public redirectUser(dest: string, ctx: NextPageContext) {
        const res = ctx.res;
        if (res) {
            res.writeHead(302, {Location: dest});
            res.end();
        } else {
            Router.push(dest);
        }
    }
}

export default NavService;
