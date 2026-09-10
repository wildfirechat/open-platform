<template>
    <ResourcePage :config="config"/>
</template>

<script>
import ResourcePage from '@/components/common/ResourcePage';
import { RESOURCE_TYPES } from '@/config/resourceTypes';

export default {
    name: 'Channel',
    components: { ResourcePage },
    data() {
        return {
            config: RESOURCE_TYPES.channel
        };
    }
};
</script>
