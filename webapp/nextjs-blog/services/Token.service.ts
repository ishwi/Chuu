import {NextPageContext} from 'next/types';


import FetchService from '../services/Fetch.service';
import NavService from '../services/Nav.service';

class TokenService {
    public saveToken(token: string) {

        localStorage.setItem('token', token);
        return Promise.resolve();
    }

    public deleteToken() {
        localStorage.remove('token');
        return;
    }

    public checkAuthToken(token: string): Promise<any> {
        return FetchService.isofetchAuthed(`/auth/validate`, {token}, 'POST');
    }

    /**
     * Runs on both client and server side in the getInitialProps static.
     * This decides whether the request is from client or server, which
     * is important as the URL's will be different due to the Docker
     * container network
     * @param ctx
     */
    public async authenticateTokenSsr(ctx: NextPageContext) {
        const token = localStorage.get('token');

        const response = await this.checkAuthToken(token);
        if (!response.success) {
            const navService = new NavService();
            this.deleteToken();
            navService.redirectUser('/?l=t', ctx);
        }
    }
}

export default TokenService;
