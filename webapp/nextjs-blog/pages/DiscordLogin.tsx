import {useRouter} from 'next/router'
import {useAuth} from "../services/Auth.context";
import FetchService from "../services/Fetch.service";
import TokenService from "../services/Token.service";
import {useGlobalMessaging} from "../services/GlobalMessaging.context";
import {InferGetServerSidePropsType} from "next";
import React from 'react';

function DiscordLogin({success}: InferGetServerSidePropsType<typeof getServerSideProps>) {
    const [messageState, messageDispatch] = useGlobalMessaging();
    const router = useRouter();
    console.log(success)

    const [authState, authDispatch] = useAuth();
    if (success && success.status) {
        const tokenService = new TokenService();
        tokenService.saveToken(success.token);
        router.push('/commands');
    } else {
    }
    return (
        <div>
            Dick {"" + success.status}
        </div>
    );
}

export const getServerSideProps = async (context) => {
    const {uuid} = context.query;


    const success = await FetchService.isofetch(
        `/discord/login?uuid=${uuid}`, null,
        'POST'
    )
        .then((res: any) => {
            console.log(res)
            console.log(res.access_token)
            if (res) {
                return {status: true, token: res.access_token};
            } else {
                return {status: false, token: null};
            }
        })
        .catch(x => ({status: false, token: null}));
    console.log(success)
    return {
        props: {
            success
        },
    }
}
export default DiscordLogin;

