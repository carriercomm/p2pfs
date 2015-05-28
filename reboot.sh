#!/bin/bash

# List of ODroids"
ODROIDS="192.168.1.100
192.168.1.101
192.168.1.102
192.168.1.103
192.168.1.104
192.168.1.105
192.168.1.106
192.168.1.107
192.168.1.108
192.168.1.109
192.168.1.110
192.168.1.111
192.168.1.112
192.168.1.113
192.168.1.114
192.168.1.115
192.168.1.116"

echo "Attempt to reboot all ODroids"
for entry in $ODROIDS; do
  echo "Reboot $entry"
  ssh -i ~/.ssh/odroid root@$entry "reboot"
done
echo "Rebooting done"