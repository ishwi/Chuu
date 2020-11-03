import {GetStaticProps,} from 'next'
import useSWR from 'swr';

export default function ImageQueueList() {
    const {data, error} = useSWR(['/api/user'], fetch)

    if (error) return <div>failed to load</div>
    if (!data) return <div>loading...</div>
    return <div>hello {data}!</div>
}

export const getStaticProps: GetStaticProps = async (context) => {
    // Call an external API endpoint to get posts
    console.log('you there')
    const res = await fetch('http://localhost:8080/commands')
    const commands = await res.json()
    console.log(commands)
    // By returning { props: posts }, the Blog component
    // will receive `posts` as a prop at build time
    return {
        props: {
            commands,
        },
        fallback: true,
        revalidate: 10000000
    }
}
